package com.southchurch.my.dto.drive;

import java.time.Instant;
import java.util.List;

import lombok.Getter;

@Getter
public class DriveEntryPointsResponse {

    private final String subjectEmail;
    private final List<String> effectiveIdentities;
    private final List<DriveEntryPoint> entryPoints;
    private final int totalAccessibleItems;
    private final Instant indexedAt;

    public DriveEntryPointsResponse(String subjectEmail, List<String> effectiveIdentities, List<DriveEntryPoint> entryPoints,
            int totalAccessibleItems, Instant indexedAt) {
        this.subjectEmail = subjectEmail;
        this.effectiveIdentities = effectiveIdentities;
        this.entryPoints = entryPoints;
        this.totalAccessibleItems = totalAccessibleItems;
        this.indexedAt = indexedAt;
    }
}