package com.southchurch.my.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.services.GoogleWorkspaceService;

import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
public class GroupController {

    @Autowired
    private GoogleWorkspaceService workspaceService;

    @GetMapping("/groups")
    public ResponseEntity<?> getGroups(@AuthenticationPrincipal Jwt principal) {
        try {
            return ResponseEntity.ok(workspaceService.listGroups());
        } catch (IOException | GeneralSecurityException e) {
            return ResponseEntity.status(500).body("Error retrieving groups: " + e.getMessage());
        }
    }
}