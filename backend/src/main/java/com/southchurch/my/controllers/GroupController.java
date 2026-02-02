package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.api.services.directory.model.Group;
import com.southchurch.my.services.GetGroupsService;

import java.util.List;

@RestController
public class GroupController {

    private final GetGroupsService getGroupsService;

    // Constructor injection
    public GroupController(GetGroupsService getGroupsService) {
        this.getGroupsService = getGroupsService;
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Group>> getGroups(@AuthenticationPrincipal Jwt principal) {
        return getGroupsService.execute(null);
    }
}
