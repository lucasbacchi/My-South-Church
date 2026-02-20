package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetGroupByIdService implements Query<String, Group> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupByIdService.class);

    public GetGroupByIdService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Executes a Google Workspace Directory API call to fetch a group by ID.
     *
     * @param groupId the ID of the group to fetch
     * @return a ResponseEntity containing the fetched group, or an error if the call fails
     * @throws IllegalArgumentException if the groupId is null or empty
     */
    @Override
    public ResponseEntity<Group> execute(String groupId) {

        logger.info("Executing " + getClass() + " input : " + groupId);

        if (groupId == null || groupId.trim().isEmpty()) {
            throw new IllegalArgumentException("groupId must not be blank");
        }

        return ResponseEntity.status(HttpStatus.OK).body(fetchGroup(groupId.trim()));
    }

    /**
     * Fetches a group by ID from the Google Workspace Directory API.
     *
     * @param id the ID of the group to fetch
     * @return the fetched group, or throws a GoogleWorkspaceException if the call fails
     * @throws GoogleWorkspaceException if the call fails
     */
    private Group fetchGroup(String id) {
       try {
            return directory.groups().get(id).execute();

        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while fetching group: " + id;

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while fetching group: " + id,
                    502,
                    e
            );
        }
    }

}
