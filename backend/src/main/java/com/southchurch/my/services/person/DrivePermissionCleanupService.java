package com.southchurch.my.services.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.Drive;

@Service
public class DrivePermissionCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(DrivePermissionCleanupService.class);

    private final Drive driveClient;

    public DrivePermissionCleanupService(Drive driveClient) {
        this.driveClient = driveClient;
    }

    @Async
    public void deletePermissionAsync(String fileId, String permissionId, String email) {
        if (fileId == null || permissionId == null) {
            return;
        }

        try {
            driveClient.permissions()
                    .delete(fileId, permissionId)
                    .setSupportsAllDrives(true)
                    .execute();
            logger.info("[GoogleAccountVerification] Cleaned up test permission for {}", email);
        } catch (Exception cleanupError) {
            logger.warn("[GoogleAccountVerification] Failed to cleanup test permission for {}: {}",
                    email, cleanupError.getMessage());
        }
    }
}
