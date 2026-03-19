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
import com.southchurch.my.dto.group.GroupAliasCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class RemoveGroupAliasService implements Command<GroupAliasCommand, Void> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(RemoveGroupAliasService.class);

    public RemoveGroupAliasService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<Void> execute(GroupAliasCommand input) {

        logger.info("Executing " + getClass() + " input : " + input);

        if (input == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String groupKey = safe(input.getGroupKey());
        String alias = safe(input.getAlias()).toLowerCase();

        if (groupKey.isEmpty())
            throw new IllegalArgumentException("groupKey must not be blank");
        if (alias.isEmpty())
            throw new IllegalArgumentException("alias must not be blank");

        try {
            directory.groups().aliases().delete(groupKey, alias).execute();
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException("Alias or group not found.", 404, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while removing alias";
            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException("Google Workspace call failed while removing alias", 502, e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

}
