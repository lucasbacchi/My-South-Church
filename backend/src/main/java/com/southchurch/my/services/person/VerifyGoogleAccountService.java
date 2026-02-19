package com.southchurch.my.services.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Permission;

/**
 * Verifies if an email address is associated with a Google Account
 * by attempting to share a test file with the email (notifications disabled).
 * 
 * Requires visitor sharing to be disabled in Google Workspace Admin.
 * When disabled, sharing only succeeds if the recipient has a Google Account.
 */
@Service
public class VerifyGoogleAccountService {

    private static final Logger logger = LoggerFactory.getLogger(VerifyGoogleAccountService.class);

    private final Drive driveClient;

    @Value("${google.drive.test-file-id}")
    private String testFileId;

    public VerifyGoogleAccountService(Drive driveClient) {
        this.driveClient = driveClient;
    }

    /**
     * Verifies if an email address is associated with a Google Account.
     * 
     * @param email The email address to verify
     * @return true if Google Account exists, false if not, null if unable to
     *         determine
     */
    public Boolean verify(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        try {
            Permission permission = new Permission()
                    .setType("user")
                    .setRole("reader")
                    .setEmailAddress(email.trim());

            // Attempt to share with notifications disabled
            driveClient.permissions()
                    .create(testFileId, permission)
                    .setSendNotificationEmail(false)
                    .setSupportsAllDrives(true)
                    .execute();

            // If successful, the email is a Google Account
            logger.info("[GoogleAccountVerification] Email {} is a Google Account", email);

            // Remove the permission immediately to keep the file clean
            try {
                String permissionId = getPermissionId(email);
                if (permissionId != null) {
                    driveClient.permissions()
                            .delete(testFileId, permissionId)
                            .setSupportsAllDrives(true)
                            .execute();
                    logger.info("[GoogleAccountVerification] Cleaned up test permission for {}", email);
                }
            } catch (Exception cleanupError) {
                logger.warn("[GoogleAccountVerification] Failed to cleanup test permission for {}: {}",
                        email, cleanupError.getMessage());
                // Non-critical, continue
            }

            return true;

        } catch (Exception e) {
            String errorMessage = e.getMessage();

            // Check for specific errors that indicate non-Google Account
            if (errorMessage != null && (errorMessage.contains("notFound") ||
                    errorMessage.contains("invalid") ||
                    errorMessage.contains("sharingOutsideDomain") ||
                    errorMessage.contains("visitorSharingDisabled"))) {

                logger.info("[GoogleAccountVerification] Email {} is not a Google Account: {}",
                        email, errorMessage);
                return false;
            }

            // Other errors are indeterminate
            logger.error("[GoogleAccountVerification] Unable to verify email {}: {}",
                    email, errorMessage, e);
            return null;
        }
    }

    /**
     * Get the permission ID for a specific email address.
     */
    private String getPermissionId(String email) {
        try {
            var permissions = driveClient.permissions()
                    .list(testFileId)
                    .setSupportsAllDrives(true)
                    .setFields("permissions(id,emailAddress)")
                    .execute()
                    .getPermissions();

            if (permissions != null) {
                for (Permission p : permissions) {
                    if (email.equalsIgnoreCase(p.getEmailAddress())) {
                        return p.getId();
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("[GoogleAccountVerification] Failed to get permission ID for {}: {}",
                    email, e.getMessage());
        }
        return null;
    }
}
