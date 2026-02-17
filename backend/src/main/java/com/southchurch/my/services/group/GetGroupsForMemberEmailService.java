package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;

@Service
public class GetGroupsForMemberEmailService implements Query<String, List<Group>> {

    private final Directory directory;

    // Inject the domain from application.properties
    @Value("${google.workspace.domain}")
    private String domain;

    public GetGroupsForMemberEmailService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<List<Group>> execute(String memberEmail) {
        System.out.println("memberEmail: " + memberEmail);
        if (memberEmail == null || memberEmail.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        try {
            List<Group> groups = listGroupsForMember(memberEmail);
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        } catch (GoogleJsonResponseException e) {
            System.out.println("Google API status: " + e.getStatusCode());
            System.out.println("Google API details: " + e.getDetails());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    private List<Group> listGroupsForMember(String memberEmail) throws IOException {
        List<Group> all = new ArrayList<>();
        String pageToken = null;

        do {
            Groups result = directory.groups()
                    .list()
                    .setDomain(domain)
                    .setUserKey(memberEmail)
                    .setPageToken(pageToken)
                    .execute();
            List<Group> page = result.getGroups();
            if (page != null)
                all.addAll(page);

            pageToken = result.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());

        return all;
    }

}
