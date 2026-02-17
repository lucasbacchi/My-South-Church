package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.Members;
import com.southchurch.my.Query;

@Service
public class GetMembersByGroupIdService implements Query<String, List<Member>> {

    private final Directory directory;

    public GetMembersByGroupIdService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<List<Member>> execute(String groupId) {

        if (groupId == null || groupId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        try {
            List<Member> members = listMembers(groupId);
            return ResponseEntity.status(HttpStatus.OK).body(members);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }

    }

    private List<Member> listMembers(String groupId) throws IOException {
        List<Member> all = new ArrayList<>();
        String pageToken = null;

        do {
            Members result = directory.members()
                    .list(groupId)
                    .setPageToken(pageToken)
                    .execute();

            List<Member> page = result.getMembers();
            if (page != null)
                all.addAll(page);

            pageToken = result.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());

        return all;
    }
}
