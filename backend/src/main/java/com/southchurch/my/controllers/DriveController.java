package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.dto.drive.DriveEntryPointsResponse;
import com.southchurch.my.dto.drive.DriveIndexStatusResponse;
import com.southchurch.my.services.drive.DriveAccessIndexService;
import com.southchurch.my.services.drive.ResolveDriveEntryPointsService;

@RestController
@RequestMapping("/drive")
public class DriveController {

    private final DriveAccessIndexService driveAccessIndexService;
    private final ResolveDriveEntryPointsService resolveDriveEntryPointsService;

    public DriveController(
            DriveAccessIndexService driveAccessIndexService,
            ResolveDriveEntryPointsService resolveDriveEntryPointsService) {
        this.driveAccessIndexService = driveAccessIndexService;
        this.resolveDriveEntryPointsService = resolveDriveEntryPointsService;
    }

    @GetMapping("/index/status")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<DriveIndexStatusResponse> getIndexStatus() {
        var snapshot = driveAccessIndexService.getSnapshot();
        boolean ready = driveAccessIndexService.isReady();
        boolean inProgress = driveAccessIndexService.isIndexingInProgress();

        int itemCount = inProgress && !ready
                ? driveAccessIndexService.getInProgressItemCount()
                : snapshot.itemsById().size();

        int identityCount = inProgress && !ready
                ? driveAccessIndexService.getInProgressIdentityCount()
                : snapshot.itemIdsByIdentity().size();

        String lastError = driveAccessIndexService.getLastIndexError();

        String message;
        if (ready) {
            message = "Drive index is ready";
        } else if (inProgress) {
            message = "Drive index is building";
        } else if (lastError != null && !lastError.isBlank()) {
            message = "Drive index is not ready; last run failed";
        } else {
            message = "Drive index is not ready yet. It will build on schedule, startup, or first query.";
        }

        return ResponseEntity.ok(new DriveIndexStatusResponse(
                ready,
                inProgress,
                snapshot.indexedAt(),
                driveAccessIndexService.getCurrentRunStartedAt(),
                itemCount,
                identityCount,
                lastError,
                message));
    }

    @PostMapping("/index/refresh")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<DriveIndexStatusResponse> refreshIndex() {
        boolean accepted = driveAccessIndexService.requestRebuildAsync("manual-api");
        var snapshot = driveAccessIndexService.getSnapshot();

        return ResponseEntity.accepted().body(new DriveIndexStatusResponse(
                driveAccessIndexService.isReady(),
                driveAccessIndexService.isIndexingInProgress(),
                snapshot.indexedAt(),
            driveAccessIndexService.getCurrentRunStartedAt(),
                snapshot.itemsById().size(),
                snapshot.itemIdsByIdentity().size(),
            driveAccessIndexService.getLastIndexError(),
            accepted ? "Drive index refresh accepted" : "Drive index refresh already in progress"));
    }

    @GetMapping("/entry-points/{email}")
    @PreAuthorize("@authorizationService.canAccessDriveEntryPoints(authentication, #email)")
    public ResponseEntity<DriveEntryPointsResponse> getEntryPointsForEmail(@PathVariable String email) {
        return resolveDriveEntryPointsService.execute(email);
    }

    @GetMapping("/entry-points/me")
    public ResponseEntity<DriveEntryPointsResponse> getMyEntryPoints(@AuthenticationPrincipal Jwt principal) {
        String email = principal.getClaimAsString("email");
        return resolveDriveEntryPointsService.execute(email);
    }
}