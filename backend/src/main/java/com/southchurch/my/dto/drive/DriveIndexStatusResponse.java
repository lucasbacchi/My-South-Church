package com.southchurch.my.dto.drive;

import java.time.Instant;

import lombok.Getter;

@Getter
public class DriveIndexStatusResponse {

    private final boolean ready;
    private final boolean indexingInProgress;
    private final Instant indexedAt;
    private final Instant indexingStartedAt;
    private final int indexedItemCount;
    private final int indexedIdentityCount;
    private final String lastError;
    private final String message;

    public DriveIndexStatusResponse(boolean ready, boolean indexingInProgress, Instant indexedAt, Instant indexingStartedAt,
            int indexedItemCount,
            int indexedIdentityCount, String lastError, String message) {
        this.ready = ready;
        this.indexingInProgress = indexingInProgress;
        this.indexedAt = indexedAt;
        this.indexingStartedAt = indexingStartedAt;
        this.indexedItemCount = indexedItemCount;
        this.indexedIdentityCount = indexedIdentityCount;
        this.lastError = lastError;
        this.message = message;
    }
}