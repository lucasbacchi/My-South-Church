package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Member;
import com.google.api.services.groupssettings.model.Groups;
import com.southchurch.my.services.group.GetGroupByIdService;
import com.southchurch.my.services.group.GetGroupSettingsService;
import com.southchurch.my.services.group.GetGroupsForMemberEmailService;
import com.southchurch.my.services.group.GetGroupsService;
import com.southchurch.my.services.group.GetMembersByGroupIdService;

import java.util.List;


@RestController
public class GroupController {

    private final GetGroupsService getGroupsService;
    private final GetMembersByGroupIdService getMembersByGroupIdService;
    private final GetGroupsForMemberEmailService getGroupsForMemberEmailService;
    private final GetGroupByIdService getGroupByIdService;
    private final GetGroupSettingsService getGroupSettingsService;


    // Constructor injection
    public GroupController(
        GetGroupsService getGroupsService,
        GetMembersByGroupIdService getMembersByGroupIdService,
        GetGroupsForMemberEmailService getGroupsForMemberEmailService,
        GetGroupByIdService getGroupByIdService,
        GetGroupSettingsService getGroupSettingsService
    ) {
        this.getGroupsService = getGroupsService;
        this.getMembersByGroupIdService = getMembersByGroupIdService;
        this.getGroupsForMemberEmailService = getGroupsForMemberEmailService;
        this.getGroupByIdService = getGroupByIdService;
        this.getGroupSettingsService = getGroupSettingsService;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getGroups(@AuthenticationPrincipal Jwt principal) {
        return getGroupsService.execute(null);
    }

    @GetMapping("group/members/{groupId}")
    public ResponseEntity<List<Member>> getMembersByGroupId(@PathVariable String groupId, 
                                                            @AuthenticationPrincipal Jwt principal) {
        System.out.println(groupId);
        return getMembersByGroupIdService.execute(groupId);
    }
    
    /*This is to be fixed; ENDPOINT NOT WORKING PROPERLY */
    @GetMapping("/member/groups")
    public ResponseEntity<List<Group>> getGroupsForMemberId(@RequestParam String memberEmail,
                                                            @AuthenticationPrincipal Jwt principal) {
        return getGroupsForMemberEmailService.execute(memberEmail);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable String groupId, 
                                               @AuthenticationPrincipal Jwt principal) {
        return getGroupByIdService.execute(groupId);
    }
    
    @GetMapping("/group/settings")
    public ResponseEntity<Groups> getGroupSettings(@RequestParam String groupId, 
                                               @AuthenticationPrincipal Jwt principal) {
        return getGroupSettingsService.execute(groupId);
    }

}
