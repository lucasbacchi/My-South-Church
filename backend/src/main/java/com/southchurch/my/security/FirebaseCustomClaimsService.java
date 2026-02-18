package com.southchurch.my.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages Firebase custom claims for roles.
 * 
 * When a user's roles change in the database, this service syncs them
 * to the Firebase user account as custom claims. These are then included
 * in the JWT on the next token refresh.
 */
@Service
public class FirebaseCustomClaimsService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseCustomClaimsService.class);

    /**
     * Update Firebase custom claims with the user's roles.
     * Call this whenever roles are created, updated, or deleted.
     * 
     * @param firebaseUID The Firebase UID of the user
     * @param roles       List of role names (e.g., ["ADMIN", "SUPER_ADMIN"])
     */
    public void syncRolesToFirebase(String firebaseUID, List<String> roles) {
        if (firebaseUID == null || firebaseUID.isBlank()) {
            logger.warn("[Firebase] Skipping role sync: no Firebase UID");
            return;
        }

        try {
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);

            FirebaseAuth.getInstance().setCustomUserClaims(firebaseUID, claims);
        } catch (FirebaseAuthException e) {
            logger.error("[Firebase] Failed to sync roles for UID {}: {}", firebaseUID, e.getMessage(), e);
            throw new RuntimeException("Failed to sync roles to Firebase: " + e.getMessage(), e);
        }
    }

    /**
     * Clear all custom claims for a user (e.g., on account deletion).
     */
    public void clearRoles(String firebaseUID) {
        if (firebaseUID == null || firebaseUID.isBlank()) {
            return;
        }

        try {
            FirebaseAuth.getInstance().setCustomUserClaims(firebaseUID, null);
        } catch (FirebaseAuthException e) {
            logger.error("[Firebase] Failed to clear roles for UID {}: {}", firebaseUID, e.getMessage());
        }
    }
}
