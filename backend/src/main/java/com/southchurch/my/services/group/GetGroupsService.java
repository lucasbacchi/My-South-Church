package com.southchurch.my.services.group;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.services.cloudidentity.v1.CloudIdentity;
import com.google.api.services.cloudidentity.v1.model.LookupGroupNameResponse;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Groups;
import com.southchurch.my.Query;

@Service
public class GetGroupsService implements Query<Void, List<Group>> {

    private final Directory directory;
    private final CloudIdentity ciService;

    @Value("${google.workspace.domain}")
    private String domain;

    // Constructor injection of the Directory bean
    public GetGroupsService(Directory directory, CloudIdentity ciService) {
        this.directory = directory;
        this.ciService = ciService;
    }

    /**
     * Executes the query to retrieve a list of groups.
     * 
     * @return A ResponseEntity containing a list of Group objects. If an error
     *         occurs during the execution of the query, a ResponseEntity with a
     *         status of 500 and an empty list is returned.
     */
    @Override
    public ResponseEntity<List<Group>> execute(Void input) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(listGroups());

        } catch (IOException | GeneralSecurityException e) {

            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * Retrieves a list of groups from the configured Google Workspace domain.
     *
     * @return A list of Group objects.
     * @throws IOException              If an I/O error occurs while executing the
     *                                  query.
     * @throws GeneralSecurityException If an authentication error occurs while
     *                                  executing the query.
     */
    public List<Group> listGroups() throws IOException, GeneralSecurityException {

        Groups result = directory
                .groups()
                .list()
                .setDomain(domain)
                .execute();
        return result.getGroups();
    }

    public boolean isMemberOfGroup(String userEmail, String groupEmail) throws IOException, GeneralSecurityException {
        try {
            return directory.members().hasMember(userEmail, groupEmail)
                    .execute()
                    .getIsMember();
        } catch (IOException e) {
            System.err.println("Error checking group membership: " + e.getMessage());
            return false; // Fail safe: access denied if we can't check
        }
    }

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

    public String runConnectivityTest() {
        try {
            // Try to fetch just ONE group from your domain
            var groups = directory.groups().list()
                    .setDomain("southchurch.com")
                    .setMaxResults(1)
                    .execute();

            if (groups.getGroups() == null || groups.getGroups().isEmpty()) {
                return "✅ Success! Connected to Google, but found 0 groups.";
            }

            return "✅ Success! Connected and found a group.";

        } catch (Exception e) {
            // If this fails, the Service Account does NOT have permission
            return "❌ FAILED: " + e.getMessage();
        }
    }

}
