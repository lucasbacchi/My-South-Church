package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;

@Service
public class GetGroupsForMemberIdService implements Query<String, List<Group>> {

    private final Directory directory;

    public GetGroupsForMemberIdService(Directory directory) {
        this.directory = directory;
    }

    @Override
    public ResponseEntity<List<Group>> execute(String memberId) {
        if (memberId == null || memberId.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        }

        try{
            List<Group> groups = listGroupsForMember(memberId);
            return ResponseEntity.status(HttpStatus.OK).body(groups);
        }
        catch(IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    private List<Group> listGroupsForMember(String memberId) throws IOException {
        List<Group> all = new ArrayList<>();
        String pageToken = null;

        do{
            Groups result = directory.groups()
                .list()
                .setUserKey(memberId)
                .setPageToken(pageToken)
                .execute();
            List<Group> page = result.getGroups();
            if(page != null) all.addAll(page);
            // if(result.getGroups() != null) all.addAll(result.getGroups());

            pageToken = result.getNextPageToken();
        } while(pageToken != null && !pageToken.isBlank());

        return all;
    }

}
