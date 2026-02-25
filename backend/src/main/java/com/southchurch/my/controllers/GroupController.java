package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Member;
import com.google.api.services.groupssettings.model.Groups;
import com.southchurch.my.dto.AddMemberToGroupCommand;
import com.southchurch.my.dto.BulkMembershipRequest;
import com.southchurch.my.dto.BulkMembershipResponse;
import com.southchurch.my.services.group.AddMemberToGroupService;
import com.southchurch.my.services.group.AddMembersToGroupsBulkService;
import com.southchurch.my.services.group.DeleteGroupService;
import com.southchurch.my.services.group.GetGroupByIdService;
import com.southchurch.my.services.group.GetGroupSettingsService;
import com.southchurch.my.services.group.GetGroupsForMemberEmailService;
import com.southchurch.my.services.group.GetGroupsService;
import com.southchurch.my.services.group.GetMembersByGroupIdService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class GroupController {

    private final GetGroupsService getGroupsService;
    private final GetMembersByGroupIdService getMembersByGroupIdService;
    private final GetGroupsForMemberEmailService getGroupsForMemberEmailService;
    private final GetGroupByIdService getGroupByIdService;
    private final GetGroupSettingsService getGroupSettingsService;
    private final AddMemberToGroupService addMemberToGroupService;
    private final AddMembersToGroupsBulkService addMembersToGroupsBulkService;
    private final DeleteGroupService deleteGroupService;

    // Constructor injection
    public GroupController(
            GetGroupsService getGroupsService,
            GetMembersByGroupIdService getMembersByGroupIdService,
            GetGroupsForMemberEmailService getGroupsForMemberEmailService,
            GetGroupByIdService getGroupByIdService,
            GetGroupSettingsService getGroupSettingsService,
            AddMemberToGroupService addMemberToGroupService,
            AddMembersToGroupsBulkService addMembersToGroupsBulkService,
            DeleteGroupService deleteGroupService) {
        this.getGroupsService = getGroupsService;
        this.getMembersByGroupIdService = getMembersByGroupIdService;
        this.getGroupsForMemberEmailService = getGroupsForMemberEmailService;
        this.getGroupByIdService = getGroupByIdService;
        this.getGroupSettingsService = getGroupSettingsService;
        this.addMemberToGroupService = addMemberToGroupService;
        this.addMembersToGroupsBulkService = addMembersToGroupsBulkService;
        this.deleteGroupService = deleteGroupService;
    }

    /**
     * Fetches all groups in the domain.
     * 
     * @return a ResponseEntity containing a list of all groups, or an error if the
     *         call fails
     */
    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getGroups(@AuthenticationPrincipal Jwt principal) {
        return getGroupsService.execute(null);
    }

    /**
     * Fetches a group by its ID.
     * 
     * @param groupId the ID of the group to fetch
     * @return a ResponseEntity containing the fetched group, or an error if the
     *         call fails
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable String groupId,
            @AuthenticationPrincipal Jwt principal) {
        return getGroupByIdService.execute(groupId);
    }

    /**
     * Fetches all members of a group with the given ID.
     * 
     * @param groupId the ID of the group to fetch members for
     * @return a ResponseEntity containing a list of all members of the group, or an
     *         error if the call fails
     */
    @GetMapping("/groups/{groupId}/members")
    public ResponseEntity<List<Member>> getMembersByGroupId(@PathVariable String groupId,
            @AuthenticationPrincipal Jwt principal) {
        return getMembersByGroupIdService.execute(groupId);
    }

    /**
     * Fetches the group settings for a group with the given ID.
     * 
     * @param groupId the ID of the group to fetch settings for
     * @return a ResponseEntity containing the fetched group settings, or an error
     *         if the call fails
     * @throws IllegalArgumentException if the groupId is null or empty
     */
    @GetMapping("/groups/{groupId}/settings")
    public ResponseEntity<Groups> getGroupSettings(@PathVariable String groupId,
            @AuthenticationPrincipal Jwt principal) {
        return getGroupSettingsService.execute(groupId);
    }

    /**
     * Fetches all groups that a member belongs to.
     * 
     * @param memberEmail the email address of the member to fetch groups for
     * @return a ResponseEntity containing a list of all groups that the member
     *         belongs to, or an error if the call fails
     * @throws IllegalArgumentException if the memberEmail is null or empty
     */
    @GetMapping("/members/{memberEmail}/groups")
    public ResponseEntity<List<Group>> getGroupsForMember(@PathVariable String memberEmail,
            @AuthenticationPrincipal Jwt principal) {
        return getGroupsForMemberEmailService.execute(memberEmail);
    }

    @PostMapping("/groups/members")
    public ResponseEntity<Member> addMemberToGroup(@RequestBody AddMemberToGroupCommand request,
            @AuthenticationPrincipal Jwt principal) {
        return addMemberToGroupService.execute(request);
    }
    
    @PostMapping("/groups/members/bulk")
    public ResponseEntity<BulkMembershipResponse> bulkAdd(@RequestBody BulkMembershipRequest request,
            @AuthenticationPrincipal Jwt principal
    ) {
        return addMembersToGroupsBulkService.execute(request);
    }
    
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId,
            @AuthenticationPrincipal Jwt principal) {
        return deleteGroupService.execute(groupId);
    }

}
