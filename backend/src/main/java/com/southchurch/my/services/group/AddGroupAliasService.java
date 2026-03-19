package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Alias;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.GroupAliasCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class AddGroupAliasService implements Command<GroupAliasCommand, Alias> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(AddGroupAliasService.class);

    public AddGroupAliasService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<Alias> execute(GroupAliasCommand input) {

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
            Alias aliasObj = new Alias();
            aliasObj.setAlias(alias);

            Alias created = directory.groups().aliases().insert(groupKey, aliasObj).execute();
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 409) {
                throw new GoogleWorkspaceException("Alias already exists: " + alias, 409, e);
            }
            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException("Group not found: " + groupKey, 404, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while adding alias";
            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException("Google Workspace call failed while adding alias", 502, e);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
