package com.southchurch.my.services.group;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.cloudidentity.v1.CloudIdentity;
import com.google.api.services.cloudidentity.v1.model.LookupGroupNameResponse;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class GetGroupsService implements Query<Void, List<Group>> {

    private final Directory directory;
    private final CloudIdentity ciService;
    private static final Logger logger = LoggerFactory.getLogger(GetGroupsService.class);

    @Value("${google.workspace.domain}")
    private String domain;

    // Constructor injection of the Directory bean
    public GetGroupsService(Directory directory, CloudIdentity ciService) {
        this.directory = directory;
        this.ciService = ciService;
    }

    /**
     * Executes a Google Workspace Directory API call to fetch all groups in the
     * domain.
     *
     * @param input the input to the query (not used)
     * @return a ResponseEntity containing the fetched groups, or an error if the
     *         call fails
     */
    @Override
    public ResponseEntity<List<Group>> execute(Void input) {

        logger.info("Executing " + getClass() + " input : " + input);

        return ResponseEntity.status(HttpStatus.OK).body(listGroups());
    }

    /**
     * Lists all groups in the domain.
     *
     * @return a list of groups, or throws a GoogleWorkspaceException if the call
     *         fails
     * @throws GoogleWorkspaceException if the call fails
     */
    public List<Group> listGroups() {

        try {
            Groups result = directory
                    .groups()
                    .list()
                    .setDomain(domain)
                    .execute();
            return result.getGroups();
        } catch (GoogleJsonResponseException e) {

            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Workspace error while listing groups";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);
        } catch (IOException e) {
            throw new GoogleWorkspaceException("Google Workspace call failed while listing groups", 502, e);
        }
    }

    /**
     * Checks if a user is a member of a group.
     *
     * @param userEmail the email address of the user to check
     * @param groupEmail the email address of the group to check
     * @return true if the user is a member of the group, false otherwise or if the call fails
     * @throws IOException if the Google Workspace Directory API call fails
     * @throws GeneralSecurityException if the Google Workspace Directory API call fails due to a security exception
     */
    public boolean isMemberOfGroup(String userEmail, String groupEmail) throws IOException, GeneralSecurityException {
        try {
            return directory.members().hasMember(userEmail, groupEmail)
                    .execute()
                    .getIsMember();
        } catch (IOException e) {
            return false; // Fail safe: access denied if we can't check
        }
    }

    /**
     * Checks if a user is a member of a group, using the Cloud Identity
     * service's checkTransitiveMembership method.
     * 
     * @param userEmail the email address of the user to check
     * @param groupEmail the email address of the group to check
     * @return true if the user is a member of the group, false otherwise or if the call fails
     * @throws IOException if the Google Workspace Directory API call fails
     */
    public boolean isMemberViaCloudIdentity(String userEmail, String groupEmail)
            throws IOException {

        LookupGroupNameResponse lookup = ciService.groups().lookup()
                .setGroupKeyId(groupEmail)
                .execute();

        String parentGroup = lookup.getName();
        String query = "member_key_id == '" + userEmail + "'";

        var response = ciService.groups().memberships()
                .checkTransitiveMembership(parentGroup)
                .setQuery(query)
                .execute();

        // If the user IS a member, response.getHasMembership() returns true.
        return response.getHasMembership() != null && response.getHasMembership();
    }

    /**
     * Tests the connectivity to the Google Workspace Directory API by fetching just one group from your domain.
     * 
     * @return a string indicating success or failure of the test, including the number of groups found.
     * @throws IOException if the Google Workspace Directory API call fails
     */
    public String runConnectivityTest() throws IOException {
        // Try to fetch just ONE group from your domain
        var groups = directory.groups().list()
                .setDomain(domain)
                .setMaxResults(1)
                .execute();

        if (groups.getGroups() == null || groups.getGroups().isEmpty()) {
            return "✅ Success! Connected to Google, but found 0 groups.";
        }

        return "✅ Success! Connected and found a group.";
    }

}
