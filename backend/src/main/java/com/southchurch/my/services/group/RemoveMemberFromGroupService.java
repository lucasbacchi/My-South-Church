package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.RemoveMemberFromGroupCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class RemoveMemberFromGroupService implements Command<RemoveMemberFromGroupCommand, Void> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(RemoveMemberFromGroupService.class);

    public RemoveMemberFromGroupService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<Void> execute(RemoveMemberFromGroupCommand input) {

        logger.info("Executing " + getClass() + " input : " + input);

        if (input == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String groupKey = safe(input.getGroupKey());
        String memberEmail = safe(input.getMemberEmail()).toLowerCase();

        if (groupKey.isEmpty())
            throw new IllegalArgumentException("groupKey must not be blank");
        if (memberEmail.isEmpty())
            throw new IllegalArgumentException("memberEmail must not be blank");

        removeMember(groupKey, memberEmail);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private void removeMember(String groupKey, String memberEmail) {
        try {
            directory.members().delete(groupKey, memberEmail).execute();

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException("Member not found in group (or group not found).", 404, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while removing member: " + memberEmail;

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);
        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while removing member: " + memberEmail,
                    502,
                    e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

}
