package com.southchurch.my.services.group;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.directory.Directory;
import com.google.api.services.directory.model.Member;
import com.southchurch.my.Command;
import com.southchurch.my.dto.group.AddMemberToGroupCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.GoogleWorkspaceException;

@Service
public class AddMemberToGroupService implements Command<AddMemberToGroupCommand, Member> {

    private final Directory directory;
    private static final Logger logger = LoggerFactory.getLogger(AddMemberToGroupService.class);

    public AddMemberToGroupService(Directory directory) {
        this.directory = directory;
    }

    /**
     * Executes a Google Workspace Directory API call to add a member to a group.
     * 
     * @param input the input to the query containing the group key, member email, and role
     * @return a ResponseEntity containing the created member, or an error if the call fails
     * @throws IllegalArgumentException if the input is null, or if the groupKey, memberEmail, or role is blank
     */
    @Override
    public ResponseEntity<Member> execute(AddMemberToGroupCommand input) {

        logger.info("Executing " + getClass() + " input : " + input);

        if (input == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String groupId = safe(input.getGroupKey());
        String memberEmail = safe(input.getMemberEmail()).toLowerCase();
        String role = safe(input.getRole());

        if (groupId.isEmpty())
            throw new IllegalArgumentException("groupId must not be blank");
        if (memberEmail.isEmpty())
            throw new IllegalArgumentException("memberEmail must not be blank");

        // default role if not specified
        String normalizedRole = role.isEmpty() ? "MEMBER" : role.toUpperCase();

        return ResponseEntity.status(HttpStatus.CREATED).body(addMember(groupId, memberEmail, normalizedRole));
    }

    /**
     * Adds a member to a group.
     * 
     * @param groupId the ID of the group to add the member to
     * @param memberEmail the email address of the member to add
     * @param role the role of the member in the group (MEMBER, MANAGER, OWNER)
     * @return the created member, or throws a GoogleWorkspaceException if the call fails
     * @throws GoogleWorkspaceException if the call fails
     */
    private Member addMember(String groupId, String memberEmail, String role) {

        try {
            Member member = new Member();
            member.setEmail(memberEmail);
            member.setRole(role); // (MEMBER, MANAGER, OWNER)

            return directory.members().insert(groupId, member).execute();
        } catch (GoogleJsonResponseException e) {
            String msg = (e.getDetails() != null && e.getDetails().getMessage() != null)
                    ? e.getDetails().getMessage()
                    : "Google Directory API error while adding member to group";

            throw new GoogleWorkspaceException(msg, e.getStatusCode(), e);

        } catch (IOException e) {
            throw new GoogleWorkspaceException(
                    "Google Workspace call failed while adding member to group",
                    502,
                    e);
        }
    }

    /**
     * Safely trims a given string to remove leading and trailing whitespace.
     * If the given string is null, an empty string is returned.
     * 
     * @param s the string to be trimmed
     * @return the trimmed string, or an empty string if the given string is null
     */
    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

}
