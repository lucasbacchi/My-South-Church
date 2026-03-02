package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.directory.model.Alias;
import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Member;
import com.google.api.services.groupssettings.model.Groups;
import com.southchurch.my.dto.group.AddMemberToGroupCommand;
import com.southchurch.my.dto.group.BulkMembershipRequest;
import com.southchurch.my.dto.group.BulkMembershipResponse;
import com.southchurch.my.dto.group.CreateGroupCommand;
import com.southchurch.my.dto.group.GroupAliasCommand;
import com.southchurch.my.dto.group.RemoveMemberFromGroupCommand;
import com.southchurch.my.dto.group.UpdateGroupSettingsCommand;
import com.southchurch.my.services.group.AddGroupAliasService;
import com.southchurch.my.services.group.AddMemberToGroupService;
import com.southchurch.my.services.group.AddMembersToGroupsBulkService;
import com.southchurch.my.services.group.CreateGroupService;
import com.southchurch.my.services.group.DeleteGroupService;
import com.southchurch.my.services.group.GetGroupAliasesService;
import com.southchurch.my.services.group.GetGroupByIdService;
import com.southchurch.my.services.group.GetGroupSettingsService;
import com.southchurch.my.services.group.GetGroupsForMemberEmailService;
import com.southchurch.my.services.group.GetGroupsService;
import com.southchurch.my.services.group.GetMembersByGroupIdService;
import com.southchurch.my.services.group.RemoveGroupAliasService;
import com.southchurch.my.services.group.RemoveMemberFromGroupService;
import com.southchurch.my.services.group.RemoveMembersFromGroupsBulkService;
import com.southchurch.my.services.group.UpdateGroupSettingsService;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;



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
    private final RemoveMemberFromGroupService removeMemberFromGroupService;
    private final CreateGroupService createGroupService;
    private final RemoveMembersFromGroupsBulkService removeMembersFromGroupsBulkService;
    private final AddGroupAliasService addGroupAliasService;
    private final GetGroupAliasesService getGroupAliasesService;
    private final RemoveGroupAliasService removeGroupAliasService;
    private final UpdateGroupSettingsService updateGroupSettingsService;

    // Constructor injection
    public GroupController(
            GetGroupsService getGroupsService,
            GetMembersByGroupIdService getMembersByGroupIdService,
            GetGroupsForMemberEmailService getGroupsForMemberEmailService,
            GetGroupByIdService getGroupByIdService,
            GetGroupSettingsService getGroupSettingsService,
            AddMemberToGroupService addMemberToGroupService,
            AddMembersToGroupsBulkService addMembersToGroupsBulkService,
            DeleteGroupService deleteGroupService,
            RemoveMemberFromGroupService removeMemberFromGroupService,
            CreateGroupService createGroupService,
            RemoveMembersFromGroupsBulkService removeMembersFromGroupsBulkService,
            AddGroupAliasService addGroupAliasService,
            GetGroupAliasesService getGroupAliasesService,
            RemoveGroupAliasService removeGroupAliasService,
            UpdateGroupSettingsService updateGroupSettingsService) {
        this.getGroupsService = getGroupsService;
        this.getMembersByGroupIdService = getMembersByGroupIdService;
        this.getGroupsForMemberEmailService = getGroupsForMemberEmailService;
        this.getGroupByIdService = getGroupByIdService;
        this.getGroupSettingsService = getGroupSettingsService;
        this.addMemberToGroupService = addMemberToGroupService;
        this.addMembersToGroupsBulkService = addMembersToGroupsBulkService;
        this.deleteGroupService = deleteGroupService;
        this.removeMemberFromGroupService = removeMemberFromGroupService;
        this.createGroupService = createGroupService;
        this.removeMembersFromGroupsBulkService = removeMembersFromGroupsBulkService;
        this.addGroupAliasService = addGroupAliasService;
        this.getGroupAliasesService = getGroupAliasesService;
        this.removeGroupAliasService = removeGroupAliasService;
        this.updateGroupSettingsService = updateGroupSettingsService;
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

    @DeleteMapping("/groups/{groupId}/members/{memberEmail}")
    public ResponseEntity<Void> removeMemberFromGroup(@PathVariable String groupId,
            @PathVariable String memberEmail, @AuthenticationPrincipal Jwt principal) {
        return removeMemberFromGroupService.execute(new RemoveMemberFromGroupCommand(groupId, memberEmail));
    }

    @PostMapping("/groups/create")
    public ResponseEntity<Group> createGroup(@RequestBody CreateGroupCommand request,
            @AuthenticationPrincipal Jwt principal
    ) {
        return createGroupService.execute(request);
    }

    @DeleteMapping("/groups/members/bulk")
    public ResponseEntity<BulkMembershipResponse> bulkRemove(@RequestBody BulkMembershipRequest request,
            @AuthenticationPrincipal Jwt principal
    ) {
        return removeMembersFromGroupsBulkService.execute(request);
    }
    
    @GetMapping("/groups/{groupId}/aliases")
    public ResponseEntity<List<String>> getGroupAliases(@PathVariable String groupId,
            @AuthenticationPrincipal Jwt principal) {
        return getGroupAliasesService.execute(groupId);
    }

    @PostMapping("/groups/{groupId}/aliases")
    public ResponseEntity<Alias> addGroupAlias(@PathVariable String groupId,
            @RequestBody GroupAliasCommand request,
            @AuthenticationPrincipal Jwt principal) {
        return addGroupAliasService.execute(new GroupAliasCommand(groupId, request.getAlias()));
    }

    @DeleteMapping("/groups/{groupId}/aliases/{alias}")
    public ResponseEntity<Void> removeGroupAlias(@PathVariable String groupId,
            @PathVariable String alias,
            @AuthenticationPrincipal Jwt principal) {
        return removeGroupAliasService.execute(new GroupAliasCommand(groupId, alias));
    }

    @PutMapping("/groups/{groupId}/settings")
    public ResponseEntity<Groups> updateGroupSettings(@PathVariable String groupId, 
            @RequestBody Groups settings,
            @AuthenticationPrincipal Jwt principal) {
        return updateGroupSettingsService.execute(new UpdateGroupSettingsCommand(groupId, settings));
    }

}
