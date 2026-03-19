package com.southchurch.my.services.drive;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class DriveAccessIndexService {

    private static final Logger logger = LoggerFactory.getLogger(DriveAccessIndexService.class);
    private static final String GOOGLE_FOLDER_MIME = "application/vnd.google-apps.folder";
    private static final String GOOGLE_SHORTCUT_MIME = "application/vnd.google-apps.shortcut";
    private static final String IDENTITY_ANYONE = "anyone";
    private static final int MAX_INDEX_PAGES = 100000;
    private static final int DRIVE_FILES_LIST_MAX_PAGE_SIZE = 1000;

    private final Drive driveClient;
    private final ExecutorService indexExecutor;
    private final ReentrantLock refreshLock = new ReentrantLock();
    private final AtomicReference<DriveAccessSnapshot> snapshotRef = new AtomicReference<>(DriveAccessSnapshot.empty());
    private final AtomicInteger inProgressItemCount = new AtomicInteger(0);
    private final AtomicInteger inProgressIdentityCount = new AtomicInteger(0);
    private final AtomicReference<Instant> currentRunStartedAt = new AtomicReference<>(null);
    private final AtomicReference<String> lastIndexError = new AtomicReference<>(null);

    @Value("${drive.index.skip-drive-ids:}")
    private String skipDriveIdsRaw;

    public DriveAccessIndexService(Drive driveClient) {
        this.driveClient = driveClient;
        this.indexExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "drive-indexer");
                t.setDaemon(true);
                return t;
            }
        });
    }

    public boolean isReady() {
        return snapshotRef.get().indexedAt() != null;
    }

    public boolean isIndexingInProgress() {
        return refreshLock.isLocked();
    }

    public DriveAccessSnapshot getSnapshot() {
        return snapshotRef.get();
    }

    public int getInProgressItemCount() {
        return inProgressItemCount.get();
    }

    public int getInProgressIdentityCount() {
        return inProgressIdentityCount.get();
    }

    public Instant getCurrentRunStartedAt() {
        return currentRunStartedAt.get();
    }

    public String getLastIndexError() {
        return lastIndexError.get();
    }

    public void ensureIndexed() {
        if (isReady()) {
            return;
        }

        requestRebuildAsync("lazy-initialization");
    }

    public boolean requestRebuildAsync(String reason) {
        if (refreshLock.isLocked()) {
            logger.info("[DriveIndexer] Async rebuild skipped; already in progress (reason={})", reason);
            return false;
        }

        indexExecutor.submit(() -> rebuildIndex(reason));
        return true;
    }

    public boolean rebuildIndex(String reason) {
        if (!refreshLock.tryLock()) {
            logger.info("[DriveIndexer] Skipping rebuild; already in progress (reason={})", reason);
            return false;
        }

        long started = System.currentTimeMillis();
        currentRunStartedAt.set(Instant.now());
        inProgressItemCount.set(0);
        inProgressIdentityCount.set(0);
        lastIndexError.set(null);

        try {
            logger.info("[DriveIndexer] Starting index rebuild (reason={})", reason);
            DriveAccessSnapshot snapshot = buildSnapshot();
            snapshotRef.set(snapshot);
            long elapsed = System.currentTimeMillis() - started;
            logger.info(
                    "[DriveIndexer] Completed index rebuild in {} ms (items={}, identities={}, grants={})",
                    elapsed,
                    snapshot.itemsById().size(),
                    snapshot.itemIdsByIdentity().size(),
                    snapshot.totalIdentityToItemMappings());
            return true;
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "Unknown indexing error";
            lastIndexError.set(msg);
            logger.error("[DriveIndexer] Index rebuild failed (reason={}): {}", reason, msg, ex);
            return false;
        } finally {
            refreshLock.unlock();
            currentRunStartedAt.set(null);
        }
    }

    private DriveAccessSnapshot buildSnapshot() {
        Map<String, DriveIndexedItem> itemsById = new HashMap<>();
        Map<String, Set<String>> itemIdsByIdentity = new HashMap<>();
        Set<String> seenPageTokens = new HashSet<>();
        Set<String> skipDriveIds = parseSkipDriveIds();
        int skippedItems = 0;

        String pageToken = null;
        int pagesProcessed = 0;
        do {
            if (pageToken != null && !seenPageTokens.add(pageToken)) {
                throw new IllegalStateException("Detected repeated Drive page token while indexing");
            }

            FileList files = listFiles(pageToken);
            int filesInPage = files.getFiles() != null ? files.getFiles().size() : 0;
            if (pagesProcessed == 0 || pagesProcessed % 25 == 0) {
                logger.info("[DriveIndexer] Page {} returned {} files (requestedPageSize={})",
                        pagesProcessed + 1,
                        filesInPage,
                        DRIVE_FILES_LIST_MAX_PAGE_SIZE);
            }

            if (files.getFiles() != null) {
                for (File file : files.getFiles()) {
                    if (file == null || file.getId() == null || file.getId().isBlank()) {
                        continue;
                    }

                    if (shouldSkipDrive(file.getDriveId(), skipDriveIds)) {
                        skippedItems++;
                        continue;
                    }

                    DriveIndexedItem indexedItem = toIndexedItem(file);
                    itemsById.put(indexedItem.id(), indexedItem);
                    indexPermissions(indexedItem.id(), file.getPermissions(), itemIdsByIdentity);
                }
            }

            pagesProcessed++;
            inProgressItemCount.set(itemsById.size());
            inProgressIdentityCount.set(itemIdsByIdentity.size());

            if (pagesProcessed > MAX_INDEX_PAGES) {
                throw new IllegalStateException("Exceeded max Drive index pages: " + MAX_INDEX_PAGES);
            }

            pageToken = files.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());

        Map<String, Set<String>> immutableIdentityMap = new HashMap<>();
        long totalMappings = 0L;
        for (Map.Entry<String, Set<String>> e : itemIdsByIdentity.entrySet()) {
            Set<String> copy = Collections.unmodifiableSet(new LinkedHashSet<>(e.getValue()));
            immutableIdentityMap.put(e.getKey(), copy);
            totalMappings += copy.size();
        }

        if (!skipDriveIds.isEmpty()) {
            logger.info("[DriveIndexer] Skipped {} items from {} configured drive IDs", skippedItems, skipDriveIds.size());
        }

        return new DriveAccessSnapshot(
                Collections.unmodifiableMap(itemsById),
                Collections.unmodifiableMap(immutableIdentityMap),
                Instant.now(),
                totalMappings);
    }

    private Set<String> parseSkipDriveIds() {
        if (skipDriveIdsRaw == null || skipDriveIdsRaw.isBlank()) {
            return Set.of();
        }

        Set<String> skipIds = new HashSet<>();
        String[] parts = skipDriveIdsRaw.split(",");
        for (String part : parts) {
            if (part == null) {
                continue;
            }
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                skipIds.add(trimmed);
            }
        }

        return skipIds;
    }

    private boolean shouldSkipDrive(String driveId, Set<String> skipDriveIds) {
        if (driveId == null || driveId.isBlank() || skipDriveIds.isEmpty()) {
            return false;
        }
        return skipDriveIds.contains(driveId);
    }

    private FileList listFiles(String pageToken) {
        try {
            return driveClient.files().list()
                    .setCorpora("allDrives")
                    .setIncludeItemsFromAllDrives(true)
                    .setSupportsAllDrives(true)
                    .setPageSize(DRIVE_FILES_LIST_MAX_PAGE_SIZE)
                    .setQ("trashed = false")
                    .setFields(
                            "nextPageToken,files(id,name,mimeType,parents,driveId,webViewLink,shortcutDetails/targetId,permissions(type,emailAddress,domain,deleted))")
                    .setPageToken(pageToken)
                    .execute();
        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Drive API error while indexing files";
            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);
        } catch (IOException e) {
            throw new GoogleWorkspaceException("Google Drive call failed while indexing files", 502, e);
        }
    }

    private DriveIndexedItem toIndexedItem(File file) {
        String mimeType = file.getMimeType() != null ? file.getMimeType() : "";
        List<String> parents = file.getParents() != null ? new ArrayList<>(file.getParents()) : List.of();
        String shortcutTargetId = file.getShortcutDetails() != null ? file.getShortcutDetails().getTargetId() : null;

        return new DriveIndexedItem(
                file.getId(),
                file.getName(),
                mimeType,
                GOOGLE_FOLDER_MIME.equals(mimeType),
                Boolean.TRUE.equals(file.getTrashed()),
                file.getWebViewLink(),
                file.getDriveId(),
                Collections.unmodifiableList(parents),
                GOOGLE_SHORTCUT_MIME.equals(mimeType),
                shortcutTargetId);
    }

    private void indexPermissions(String fileId, List<Permission> permissions, Map<String, Set<String>> itemIdsByIdentity) {
        if (permissions == null || permissions.isEmpty()) {
            return;
        }

        for (Permission permission : permissions) {
            if (permission == null || Boolean.TRUE.equals(permission.getDeleted())) {
                continue;
            }

            for (String identity : extractIdentities(permission)) {
                itemIdsByIdentity.computeIfAbsent(identity, k -> new HashSet<>()).add(fileId);
            }
        }
    }

    private List<String> extractIdentities(Permission permission) {
        if (permission.getType() == null || permission.getType().isBlank()) {
            return List.of();
        }

        String type = permission.getType().trim().toLowerCase(Locale.ROOT);
        if ("user".equals(type) || "group".equals(type)) {
            if (permission.getEmailAddress() == null || permission.getEmailAddress().isBlank()) {
                return List.of();
            }
            return List.of(permission.getEmailAddress().trim().toLowerCase(Locale.ROOT));
        }

        if ("domain".equals(type)) {
            if (permission.getDomain() == null || permission.getDomain().isBlank()) {
                return List.of();
            }
            return List.of("domain:" + permission.getDomain().trim().toLowerCase(Locale.ROOT));
        }

        if ("anyone".equals(type)) {
            return List.of(IDENTITY_ANYONE);
        }

        return List.of();
    }

    public Set<String> getItemIdsForIdentity(String identity) {
        if (identity == null || identity.isBlank()) {
            return Set.of();
        }

        String normalized = identity.trim().toLowerCase(Locale.ROOT);
        Set<String> ids = snapshotRef.get().itemIdsByIdentity().get(normalized);
        return ids != null ? ids : Set.of();
    }

    public record DriveIndexedItem(
            String id,
            String name,
            String mimeType,
            boolean folder,
            boolean trashed,
            String webViewLink,
            String driveId,
            List<String> parentIds,
            boolean shortcut,
            String shortcutTargetId) {
    }

    public record DriveAccessSnapshot(
            Map<String, DriveIndexedItem> itemsById,
            Map<String, Set<String>> itemIdsByIdentity,
            Instant indexedAt,
            long totalIdentityToItemMappings) {

        public static DriveAccessSnapshot empty() {
            return new DriveAccessSnapshot(Map.of(), Map.of(), null, 0);
        }
    }
}