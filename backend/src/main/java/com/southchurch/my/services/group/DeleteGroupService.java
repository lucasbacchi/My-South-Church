package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.southchurch.my.Command;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class DeleteGroupService implements Command<String, Void> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(DeleteGroupService.class);

    public DeleteGroupService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Deletes a group using the given group key.
     *
     * @param groupKey the key of the group to delete
     * @return a ResponseEntity containing no content, or an error if the call fails
     * @throws IllegalArgumentException if the groupKey is null or empty
     */
    @Override
    public ResponseEntity<Void> execute(String groupKey) {

        logger.info("Executing " + getClass() + " input : " + groupKey);

        if (groupKey == null || groupKey.trim().isEmpty()) {
            throw new IllegalArgumentException("groupKey must not be blank");
        }

        delete(groupKey.trim());

        return ResponseEntity.noContent().build(); // 204
    }

    /**
     * Deletes a group using the given group key.
     *
     * @param groupKey the key of the group to delete
     * @throws GoogleWorkspaceException if the call fails
     */
    private void delete(String groupKey) {

        try {
            directory.groups().delete(groupKey).execute();

        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while deleting group: " + groupKey;

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while deleting group: " + groupKey,
                    502,
                    e);
        }
    }

}
