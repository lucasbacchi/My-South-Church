package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;
import com.southchurch.my.dto.group.EffectiveIdentitiesResponse;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetEffectiveIdentitiesService implements Query<String, EffectiveIdentitiesResponse> {

    private static final Logger logger = LoggerFactory.getLogger(GetEffectiveIdentitiesService.class);

    private final Directory directory;

    @Value("${google.workspace.domain}")
    private String domain;

    public GetEffectiveIdentitiesService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<EffectiveIdentitiesResponse> execute(String subjectEmail) {
        logger.info("Executing {} input: {}", getClass(), subjectEmail);

        if (subjectEmail == null || subjectEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        String normalizedEmail = normalizeEmail(subjectEmail);

        LinkedHashSet<String> identities = new LinkedHashSet<>();
        LinkedHashSet<String> visited = new LinkedHashSet<>();
        Deque<String> queue = new ArrayDeque<>();

        identities.add(normalizedEmail);
        queue.add(normalizedEmail);

        while (!queue.isEmpty()) {
            String currentEmail = queue.removeFirst();

            if (!visited.add(currentEmail)) {
                continue;
            }

            List<Group> parentGroups = listParentGroups(currentEmail);
            for (Group parentGroup : parentGroups) {
                if (parentGroup == null || parentGroup.getEmail() == null || parentGroup.getEmail().isBlank()) {
                    continue;
                }

                String parentGroupEmail = normalizeEmail(parentGroup.getEmail());
                if (identities.add(parentGroupEmail) && !visited.contains(parentGroupEmail)) {
                    queue.addLast(parentGroupEmail);
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new EffectiveIdentitiesResponse(normalizedEmail, new ArrayList<>(identities)));
    }

    private List<Group> listParentGroups(String memberKey) {
        try {
            List<Group> groups = new ArrayList<>();
            String pageToken = null;

            do {
                Groups response = directory.groups()
                        .list()
                        .setDomain(domain)
                        .setUserKey(memberKey)
                        .setPageToken(pageToken)
                        .execute();

                if (response.getGroups() != null) {
                    groups.addAll(response.getGroups());
                }

                pageToken = response.getNextPageToken();
            } while (pageToken != null && !pageToken.isBlank());

            return groups;
        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while listing effective parent groups";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);
        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while listing effective parent groups",
                    502,
                    e);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}