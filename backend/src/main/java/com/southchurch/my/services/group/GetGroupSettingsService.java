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
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetGroupSettingsService implements Query<String, Groups> {

    private final Groupssettings groupSettings;
    private final GetGroupByIdService getGroupByIdService;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupSettingsService.class);

    public GetGroupSettingsService(Groupssettings groupSettings, GetGroupByIdService getGroupByIdService) {
        this.groupSettings = groupSettings;
        this.getGroupByIdService = getGroupByIdService;
    }

    /**
     * Executes a Google Workspace Directory API call to fetch the group settings
     * for a group using the given ID or email.
     *
     * @param groupIdOrEmail the ID or email of the group to fetch settings for
     * @return a ResponseEntity containing the fetched group settings, or an error
     *         if the call fails
     * @throws IllegalArgumentException if the groupIdOrEmail is null or empty
     */
    @Override
    public ResponseEntity<Groups> execute(String groupIdOrEmail) {

        logger.info("Executing " + getClass() + " input : " + groupIdOrEmail);

        if (groupIdOrEmail == null || groupIdOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("groupId must not be blank");
        }

        String idOrEmail = groupIdOrEmail.trim();
        String groupEmail = resolveGroupEmail(idOrEmail);

        return ResponseEntity.status(HttpStatus.OK).body(fetchSettings(groupEmail));
    }

    /**
     * Resolves the group email from the given ID or email.
     * 
     * @param idOrEmail the ID or email of the group to resolve the email for
     * @return the resolved group email
     * @throws GoogleWorkspaceException if the group id cannot be resolved to an
     *                                  email
     */
    private String resolveGroupEmail(String idOrEmail) {
        if (idOrEmail.contains("@")) {
            return idOrEmail;
        }

        ResponseEntity<Group> resp = getGroupByIdService.execute(idOrEmail);

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            int code = resp.getStatusCode().value();
            throw new GoogleWorkspaceException("Failed to resolve group email from id", code, null);
        }

        String email = resp.getBody().getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new GoogleWorkspaceException("Resolved group has no email", 502, null);
        }

        return email.trim();
    }

    /**
     * Fetches the group settings for the given group email.
     * 
     * @param groupEmail the email of the group to fetch the settings for
     * @return the fetched group settings, or throws a GoogleWorkspaceException if
     *         the call fails
     * @throws GoogleWorkspaceException if the call fails
     */
    private Groups fetchSettings(String groupEmail) {
        try {

            // Fetch the settings using the group email
            return groupSettings.groups().get(groupEmail).execute();

        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Groups Settings API error for group: " + groupEmail;

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Groups Settings API call failed for group: " + groupEmail,
                    502,
                    e);
        }
    }

}
