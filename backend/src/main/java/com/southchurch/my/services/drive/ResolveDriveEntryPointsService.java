package com.southchurch.my.services.drive;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.drive.DriveEntryPoint;
import com.southchurch.my.dto.drive.DriveEntryPointsResponse;
import com.southchurch.my.dto.group.EffectiveIdentitiesResponse;
import com.southchurch.my.services.drive.DriveAccessIndexService.DriveAccessSnapshot;
import com.southchurch.my.services.drive.DriveAccessIndexService.DriveIndexedItem;
import com.southchurch.my.services.group.GetEffectiveIdentitiesService;

@Service
public class ResolveDriveEntryPointsService implements Query<String, DriveEntryPointsResponse> {

    private static final Logger logger = LoggerFactory.getLogger(ResolveDriveEntryPointsService.class);
    private static final String IDENTITY_ANYONE = "anyone";

    private final DriveAccessIndexService driveAccessIndexService;
    private final GetEffectiveIdentitiesService getEffectiveIdentitiesService;

    @Value("${google.workspace.domain}")
    private String workspaceDomain;

    public ResolveDriveEntryPointsService(
            DriveAccessIndexService driveAccessIndexService,
            GetEffectiveIdentitiesService getEffectiveIdentitiesService) {
        this.driveAccessIndexService = driveAccessIndexService;
        this.getEffectiveIdentitiesService = getEffectiveIdentitiesService;
    }

    @Override
    public ResponseEntity<DriveEntryPointsResponse> execute(String subjectEmail) {
        if (subjectEmail == null || subjectEmail.isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        String normalizedEmail = normalize(subjectEmail);
        if (!driveAccessIndexService.isReady()) {
            driveAccessIndexService.requestRebuildAsync("on-demand-request");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new DriveEntryPointsResponse(
                            normalizedEmail,
                            List.of(),
                            List.of(),
                            0,
                            null));
        }

        ResponseEntity<EffectiveIdentitiesResponse> identitiesResponse = getEffectiveIdentitiesService.execute(normalizedEmail);
        EffectiveIdentitiesResponse identitiesBody = identitiesResponse.getBody();

        List<String> effectiveIdentities = new ArrayList<>();
        if (identitiesBody != null && identitiesBody.getIdentities() != null) {
            for (String identity : identitiesBody.getIdentities()) {
                if (identity != null && !identity.isBlank()) {
                    effectiveIdentities.add(normalize(identity));
                }
            }
        }

        if (!effectiveIdentities.contains(normalizedEmail)) {
            effectiveIdentities.add(normalizedEmail);
        }

        String domainFromSubject = extractDomain(normalizedEmail);
        if (domainFromSubject != null) {
            effectiveIdentities.add("domain:" + domainFromSubject);
        }
        if (workspaceDomain != null && !workspaceDomain.isBlank()) {
            effectiveIdentities.add("domain:" + normalize(workspaceDomain));
        }
        effectiveIdentities.add(IDENTITY_ANYONE);

        LinkedHashSet<String> dedupedIdentities = new LinkedHashSet<>(effectiveIdentities);
        DriveAccessSnapshot snapshot = driveAccessIndexService.getSnapshot();

        LinkedHashSet<String> candidateIds = new LinkedHashSet<>();
        for (String identity : dedupedIdentities) {
            candidateIds.addAll(driveAccessIndexService.getItemIdsForIdentity(identity));
        }

        LinkedHashSet<String> normalizedCandidates = normalizeCandidates(candidateIds, snapshot.itemsById());
        LinkedHashSet<String> roots = collapseToRoots(normalizedCandidates, snapshot.itemsById());

        List<DriveEntryPoint> entryPoints = roots.stream()
                .map(snapshot.itemsById()::get)
                .filter(item -> item != null && !item.trashed())
                .map(item -> new DriveEntryPoint(
                        item.id(),
                        item.name(),
                        item.mimeType(),
                        item.folder(),
                        item.webViewLink(),
                        item.driveId(),
                        item.parentIds()))
                .sorted(Comparator.comparing(DriveEntryPoint::getName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(DriveEntryPoint::getId))
                .toList();

        logger.info("[DriveResolver] subject={} identities={} candidates={} roots={}",
                normalizedEmail,
                dedupedIdentities.size(),
                normalizedCandidates.size(),
                entryPoints.size());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new DriveEntryPointsResponse(
                        normalizedEmail,
                        new ArrayList<>(dedupedIdentities),
                        entryPoints,
                        normalizedCandidates.size(),
                        snapshot.indexedAt()));
    }

    private LinkedHashSet<String> normalizeCandidates(Set<String> candidateIds, Map<String, DriveIndexedItem> itemsById) {
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String id : candidateIds) {
            if (id == null || id.isBlank()) {
                continue;
            }

            DriveIndexedItem item = itemsById.get(id);
            if (item == null || item.trashed()) {
                continue;
            }

            if (item.shortcut() && item.shortcutTargetId() != null && itemsById.containsKey(item.shortcutTargetId())) {
                DriveIndexedItem target = itemsById.get(item.shortcutTargetId());
                if (target != null && !target.trashed()) {
                    normalized.add(target.id());
                    continue;
                }
            }

            normalized.add(item.id());
        }
        return normalized;
    }

    private LinkedHashSet<String> collapseToRoots(Set<String> candidates, Map<String, DriveIndexedItem> itemsById) {
        LinkedHashSet<String> roots = new LinkedHashSet<>();
        for (String itemId : candidates) {
            if (!hasAncestorInSet(itemId, candidates, itemsById)) {
                roots.add(itemId);
            }
        }
        return roots;
    }

    private boolean hasAncestorInSet(String itemId, Set<String> candidates, Map<String, DriveIndexedItem> itemsById) {
        DriveIndexedItem start = itemsById.get(itemId);
        if (start == null || start.parentIds() == null || start.parentIds().isEmpty()) {
            return false;
        }

        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>(start.parentIds());

        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (current == null || current.isBlank() || !visited.add(current)) {
                continue;
            }

            if (candidates.contains(current)) {
                return true;
            }

            DriveIndexedItem parent = itemsById.get(current);
            if (parent != null && parent.parentIds() != null && !parent.parentIds().isEmpty()) {
                stack.addAll(parent.parentIds());
            }
        }

        return false;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String extractDomain(String email) {
        int idx = email.indexOf('@');
        if (idx < 0 || idx == email.length() - 1) {
            return null;
        }
        return email.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}