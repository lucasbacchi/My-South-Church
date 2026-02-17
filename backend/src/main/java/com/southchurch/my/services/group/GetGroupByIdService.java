package com.southchurch.my.services.group;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.southchurch.my.Query;

@Service
public class GetGroupByIdService implements Query<String, Group> {

    private final Directory directory;

    public GetGroupByIdService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<Group> execute(String groupId) {
        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String id = groupId.trim();
        try {
            Group group = directory.groups().get(id).execute();
            return ResponseEntity.status(HttpStatus.OK).body(group);
        } catch (GoogleJsonResponseException e) {
            // 404 if group not found, 403 if permissions, etc.
            int status = e.getStatusCode();
            if (status == 404) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            if (status == 403) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
