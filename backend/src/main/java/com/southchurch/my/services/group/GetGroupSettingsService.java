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

@Service
public class GetGroupSettingsService implements Query<String, Groups> {

    private final Groupssettings groupSettings;
    private final GetGroupByIdService getGroupByIdService;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupSettingsService.class);

    public GetGroupSettingsService(Groupssettings groupSettings, GetGroupByIdService getGroupByIdService) {
        this.groupSettings = groupSettings;
        this.getGroupByIdService = getGroupByIdService;
    }

    @Override
    public ResponseEntity<Groups> execute(String groupId) {

        logger.info("Executing " + getClass() + " input : " + groupId);

        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String id = groupId.trim();
        String groupEmail = id;

        try {
            // If the input doesn't look like an email, look up the group by ID to get its
            // email
            if (!id.contains("@")) {
                ResponseEntity<Group> groupResponse = getGroupByIdService.execute(id);
                if (!groupResponse.getStatusCode().is2xxSuccessful() || groupResponse.getBody() == null) {
                    return ResponseEntity.status(groupResponse.getStatusCode()).build();
                }

                groupEmail = groupResponse.getBody().getEmail();
                if (groupEmail == null || groupEmail.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            // Fetch the settings using the group email
            Groups settings = groupSettings.groups().get(groupEmail).execute();
            return ResponseEntity.status(HttpStatus.OK).body(settings);

        } catch (GoogleJsonResponseException e) {
            if (e.getStatusCode() == 404)
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (e.getStatusCode() == 403)
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
