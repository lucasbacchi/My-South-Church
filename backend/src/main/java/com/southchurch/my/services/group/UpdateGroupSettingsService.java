package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.model.Group;
import com.google.api.services.groupssettings.Groupssettings;
import com.google.api.services.groupssettings.model.Groups;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.UpdateGroupSettingsCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class UpdateGroupSettingsService implements Command<UpdateGroupSettingsCommand, Groups> {

    private final Groupssettings groupSettings;
    private final GetGroupByIdService getGroupByIdService;
    private static final Logger logger = LoggerFactory.getLogger(UpdateGroupSettingsService.class);

    public UpdateGroupSettingsService(Groupssettings groupSettings, GetGroupByIdService getGroupByIdService) {
        this.groupSettings = groupSettings;
        this.getGroupByIdService = getGroupByIdService;
    }

    @Override
    public ResponseEntity<Groups> execute(UpdateGroupSettingsCommand input) {

        logger.info("Executing " + getClass() + " input : " + input);

        if (input == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String groupKey = input.getGroupKey() == null ? "" : input.getGroupKey().trim();
        if (groupKey.isEmpty())
            throw new IllegalArgumentException("groupKey must not be blank");
        if (input.getSettings() == null)
            throw new IllegalArgumentException("settings must not be null");

        String groupEmail = resolveGroupEmail(groupKey);

        try {

            Groups updatedSettings = groupSettings.groups().update(groupEmail, input.getSettings()).execute();
            return ResponseEntity.status(HttpStatus.OK).body(updatedSettings);

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException("Group not found: " + groupKey, 404, e);
            }
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Groups Settings API error while updating group: " + groupEmail;
            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Groups Settings API call failed while updating group: " + groupEmail, 502, e);
        }

    }

    private String resolveGroupEmail(String idOrEmail) {

        if (idOrEmail.contains("@")) {
            return idOrEmail;
        }

        ResponseEntity<Group> resp = getGroupByIdService.execute(idOrEmail);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw new GoogleWorkspaceException("Failed to resolve group email from id", resp.getStatusCode().value(),
                    null);
        }

        String email = resp.getBody().getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new GoogleWorkspaceException("Resolved group has no email", 502, null);
        }

        return email.trim();
    }
}
