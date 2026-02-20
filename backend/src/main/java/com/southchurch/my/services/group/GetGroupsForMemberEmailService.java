package com.southchurch.my.services.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetGroupsForMemberEmailService implements Query<String, List<Group>> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupsForMemberEmailService.class);

    // Inject the domain from application.properties
    @Value("${google.workspace.domain}")
    private String domain;

    public GetGroupsForMemberEmailService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Executes a Google Workspace Directory API call to fetch all groups that a member belongs to.
     *
     * @param memberEmail the email address of the member to fetch groups for
     * @return a ResponseEntity containing the fetched groups, or an error if the call fails
     * @throws IllegalArgumentException if the memberEmail is null or empty
     */
    @Override
    public ResponseEntity<List<Group>> execute(String memberEmail) {

        logger.info("Executing " + getClass() + " input : " + memberEmail);

        if (memberEmail == null || memberEmail.trim().isEmpty()) {
           throw new IllegalArgumentException("memberEmail must not be blank");
        }

        String email = memberEmail.trim().toLowerCase();

        ensureGoogleAccountExists(email);

        return ResponseEntity.status(HttpStatus.OK).body(listGroupsForMember(memberEmail));

    }

    /**
     * Ensures that the given email address is associated with a Google Account.
     * If the email address is not associated with a Google Account, throws a GoogleWorkspaceException.
     * 
     * @param email the email address to check
     * @throws GoogleWorkspaceException if the email address is not associated with a Google Account
     */
    private void ensureGoogleAccountExists(String email) {
        try{
            directory.users().get(email).execute();
        } catch (GoogleJsonResponseException e) {

            if (e.getStatusCode() == 404) {
                throw new GoogleWorkspaceException(
                    "This member does not have a Google Account associated with their email address: " + email +
                    ". They must create/activate a Google account before group memberships can be retrieved.",
                    400,
                    e
                );
            }

            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                ? e.getDetails().getMessage()
                : "Google Directory API error while checking if user exists";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                "Google Workspace call failed while checking if user exists",
                502,
                e
            );
        }
    }

    /**
     * Lists all groups that a member belongs to.
     * 
     * @param memberEmail the email address of the member to fetch groups for
     * @return a list of groups that the member belongs to
     * @throws GoogleWorkspaceException if the call fails
     */
    private List<Group> listGroupsForMember(String memberEmail){

        try{
            
            List<Group> all = new ArrayList<>();
            String pageToken = null;
    
            do {
                Groups result = directory.groups()
                        .list()
                        .setDomain(domain)
                        .setUserKey(memberEmail)
                        .setPageToken(pageToken)
                        .execute();
                if (result.getGroups() != null)
                    all.addAll(result.getGroups());
    
                pageToken = result.getNextPageToken();
            } while (pageToken != null && !pageToken.isBlank());
    
            return all;

        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while listing groups for member";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while listing groups for member",
                    502,
                    e
            );
        }
        
    }

}
