package com.southchurch.my.services.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
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
    private final DrivePermissionCleanupService cleanupService;

    @Value("${google.drive.test-file-id}")
    private String testFileId;

    public VerifyGoogleAccountService(Drive driveClient, DrivePermissionCleanupService cleanupService) {
        this.driveClient = driveClient;
        this.cleanupService = cleanupService;
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
            Permission created = driveClient.permissions()
                    .create(testFileId, permission)
                    .setSendNotificationEmail(false)
                    .setSupportsAllDrives(true)
                    .setFields("id")
                    .execute();

            // If successful, the email is a Google Account
            logger.info("[GoogleAccountVerification] Email {} is a Google Account", email);

            // Remove the permission asynchronously to return faster
            if (created != null && created.getId() != null) {
                cleanupService.deletePermissionAsync(testFileId, created.getId(), email);
            }

            return true;

        } catch (GoogleJsonResponseException e) {
            int statusCode = e.getStatusCode();
            String errorMessage = e.getDetails() != null ? e.getDetails().getMessage() : e.getMessage();
            String reason = null;
            if (e.getDetails() != null && e.getDetails().getErrors() != null && !e.getDetails().getErrors().isEmpty()) {
                reason = e.getDetails().getErrors().get(0).getReason();
            }

            if (isNotGoogleAccountError(statusCode, reason, errorMessage)) {
                logger.info("[GoogleAccountVerification] Email {} is not a Google Account: {}",
                        email, errorMessage);
                return false;
            }

            logger.error("[GoogleAccountVerification] Unable to verify email {}: {}",
                    email, errorMessage, e);
            return null;
        } catch (Exception e) {
            String errorMessage = e.getMessage();

            // Other errors are indeterminate
            logger.error("[GoogleAccountVerification] Unable to verify email {}: {}",
                    email, errorMessage, e);
            return null;
        }
    }

    private boolean isNotGoogleAccountError(int statusCode, String reason, String errorMessage) {
        String message = errorMessage == null ? "" : errorMessage.toLowerCase();
        String reasonLower = reason == null ? "" : reason.toLowerCase();

        if (statusCode == 400 || statusCode == 404) {
            return true;
        }

        if (statusCode == 403) {
            return reasonLower.contains("sharingoutsidedomain")
                    || reasonLower.contains("domainpolicy")
                    || message.contains("sharingoutsidedomain")
                    || message.contains("domain policy");
        }

        return reasonLower.contains("invalid")
                || reasonLower.contains("notfound")
                || reasonLower.contains("usernotfound")
                || reasonLower.contains("invalidsharingrequest")
                || message.contains("notfound")
                || message.contains("invalid")
                || message.contains("visitor sharing disabled");
    }

}
