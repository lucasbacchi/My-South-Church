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
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.CreateGroupCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class CreateGroupService implements Command<CreateGroupCommand, Group> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(CreateGroupService.class);

    public CreateGroupService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Executes a Google Workspace Directory API call to create a new group with the given name, email address and description.
     *
     * @param request the create group command containing the group name, email address and description
     * @return a ResponseEntity containing the created group, or an error if the call fails
     * @throws IllegalArgumentException if the request is null or empty
     */
    @Override
    public ResponseEntity<Group> execute(CreateGroupCommand request) {

        logger.info("Executing " + getClass() + " input : " + request);

        if (request == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String name = safe(request.getName());
        String email = safe(request.getEmail()).toLowerCase();
        String description = safe(request.getDescription());

        if (name.isEmpty())
            throw new IllegalArgumentException("name must not be blank");
        if (email.isEmpty())
            throw new IllegalArgumentException("email must not be blank");

        return ResponseEntity.status(HttpStatus.CREATED).body(createGroup(name, email, description));

    }

    /**
     * Creates a new group with the given name, email address and description.
     * 
     * @param name the name of the group to create
     * @param email the email address of the group to create
     * @param description the description of the group to create (optional)
     * @return the created group
     * @throws GoogleWorkspaceException if the call fails
     */
    private Group createGroup(String name, String email, String description) {

        try {
            Group group = new Group();
            group.setName(name);
            group.setEmail(email);
            if (!description.isEmpty()) {
                group.setDescription(description);
            }
            return directory.groups().insert(group).execute();
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 409) {
                throw new GoogleWorkspaceException("A group with that email already exists.", 409, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while creating group";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while creating group",
                    502,
                    e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
