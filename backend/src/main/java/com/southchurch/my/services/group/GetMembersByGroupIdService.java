package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.google.api.services.directory.model.Members;
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetMembersByGroupIdService implements Query<String, List<Member>> {

    private final Directory directory;
    private final static Logger logger = LoggerFactory.getLogger(GetMembersByGroupIdService.class);

    public GetMembersByGroupIdService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Executes a Google Workspace Directory API call to fetch all members of a group with the given ID.
     * 
     * @param groupId the ID of the group to fetch members for
     * @return a ResponseEntity containing the fetched members, or an error if the call fails
     * @throws IllegalArgumentException if the groupId is null or empty
     */
    @Override
    public ResponseEntity<List<Member>> execute(String groupId) {

        logger.info("Executing " + getClass() + " input : " + groupId);

        if (groupId == null || groupId.trim().isEmpty()) {
           throw new IllegalArgumentException("groupId must not be blank");
        }

        return ResponseEntity.status(HttpStatus.OK).body(listMembers(groupId));
    }

    /**
     * Lists all members of a group with the given ID.
     *
     * @param groupId the ID of the group to fetch members for
     * @return a list of members of the group, or throws a GoogleWorkspaceException if the call fails
     * @throws GoogleWorkspaceException if the call fails
     */
    private List<Member> listMembers(String groupId){

        try{
            List<Member> all = new ArrayList<>();
            String pageToken = null;

            do {
                Members result = directory.members()
                        .list(groupId)
                        .setPageToken(pageToken)
                        .execute();
    
                if (result.getMembers() != null)
                    all.addAll(result.getMembers());
    
                pageToken = result.getNextPageToken();
            } while (pageToken != null && !pageToken.isBlank());
    
            return all;

        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Workspace error while listing members for group: " + groupId;

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                "Google Workspace call failed while listing members for group: " + groupId,
                502,
                e
            );
        } 
    }
}
