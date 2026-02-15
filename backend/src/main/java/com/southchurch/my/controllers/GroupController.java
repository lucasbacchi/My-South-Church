package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.directory.model.Group;
import com.google.api.services.directory.model.Member;
import com.southchurch.my.services.group.GetGroupsForMemberIdService;
import com.southchurch.my.services.group.GetGroupsService;
import com.southchurch.my.services.group.GetMembersByGroupIdService;

import java.util.List;


@RestController
public class GroupController {

    private final GetGroupsService getGroupsService;
    private final GetMembersByGroupIdService getMembersByGroupIdService;
    private final GetGroupsForMemberIdService getGroupsForMemberIdService;


    // Constructor injection
    public GroupController(
        GetGroupsService getGroupsService,
        GetMembersByGroupIdService getMembersByGroupIdService,
        GetGroupsForMemberIdService getGroupsForMemberIdService
    ) {
        this.getGroupsService = getGroupsService;
        this.getMembersByGroupIdService = getMembersByGroupIdService;
        this.getGroupsForMemberIdService = getGroupsForMemberIdService;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getGroups(@AuthenticationPrincipal Jwt principal) {
        return getGroupsService.execute(null);
    }

    @GetMapping("/members/{groupId}")
    public ResponseEntity<List<Member>> getMembersByGroupId(@PathVariable String groupId, 
                                                            @AuthenticationPrincipal Jwt principal) {
        System.out.println(groupId);
        return getMembersByGroupIdService.execute(groupId);
    }
    
    @GetMapping("/groups/{memberId}")
    public ResponseEntity<List<Group>> getGroupsForMemberId(@PathVariable String memberId,
                                                            @AuthenticationPrincipal Jwt principal) {
        return getGroupsForMemberIdService.execute(memberId);
    }
    
}
