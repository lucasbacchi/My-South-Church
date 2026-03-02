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

    @Override
    public ResponseEntity<Member> execute(AddMemberToGroupCommand input) {

        logger.info("Executing " + getClass() + " input : " + input);

        if(input == null) {
            throw new IllegalArgumentException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        String groupId = safe(input.getGroupKey());
        String memberEmail = safe(input.getMemberEmail()).toLowerCase();
        String role = safe(input.getRole()); 

        if (groupId.isEmpty()) throw new IllegalArgumentException("groupId must not be blank");
        if (memberEmail.isEmpty()) throw new IllegalArgumentException("memberEmail must not be blank");

        // default role if not specified
        String normalizedRole = role.isEmpty() ? "MEMBER" : role.toUpperCase();

        return ResponseEntity.status(HttpStatus.CREATED).body(addMember(groupId, memberEmail, normalizedRole));
    }

    private Member addMember(String groupId, String memberEmail, String role) {
        
        try{
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
                    e
            );
        }    
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

}
