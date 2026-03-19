package com.southchurch.my.security;

import java.util.LinkedHashSet;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.southchurch.my.dto.group.EffectiveIdentitiesResponse;
import com.southchurch.my.services.group.GetEffectiveIdentitiesService;

/**
 * Authorization service for endpoint-level access control.
 * Used with @PreAuthorize annotations via SpEL.
 *
 * Example: @PreAuthorize("@authorizationService.canDeletePerson(authentication)")
 */
@Service
public class AuthorizationService {

    private final GetEffectiveIdentitiesService getEffectiveIdentitiesService;

    public AuthorizationService(GetEffectiveIdentitiesService getEffectiveIdentitiesService) {
        this.getEffectiveIdentitiesService = getEffectiveIdentitiesService;
    }

    /**
     * Check if user is SUPER_ADMIN
     */
    public boolean isSuperAdmin(Authentication auth) {
        return hasRole(auth, "SUPER_ADMIN");
    }

    /**
     * Check if user is ADMIN or SUPER_ADMIN
     */
    public boolean isAdmin(Authentication auth) {
        return hasRole(auth, "ADMIN") || hasRole(auth, "SUPER_ADMIN");
    }

    /**
     * Check if user can create people (ADMIN+)
     */
    public boolean canCreatePerson(Authentication auth) {
        return isAdmin(auth);
    }

    /**
     * Check if user can update person details (ADMIN+)
     */
    public boolean canUpdatePerson(Authentication auth) {
        return isAdmin(auth);
    }

    /**
     * Check if user can delete people (SUPER_ADMIN only)
     */
    public boolean canDeletePerson(Authentication auth) {
        return isSuperAdmin(auth);
    }

    /**
     * Drive access rule:
     * - Admins can query any email
     * - Non-admin users can only query their own email or one of their effective
     * identities (e.g. groups they belong to)
     */
    public boolean canAccessDriveEntryPoints(Authentication auth, String requestedEmail) {
        if (requestedEmail == null || requestedEmail.isBlank() || auth == null || !auth.isAuthenticated()) {
            return false;
        }

        if (isAdmin(auth)) {
            return true;
        }

        String authenticatedEmail = extractAuthenticatedEmail(auth);
        if (authenticatedEmail == null) {
            return false;
        }

        String requested = normalize(requestedEmail);
        if (authenticatedEmail.equals(requested)) {
            return true;
        }

        try {
            EffectiveIdentitiesResponse identities = getEffectiveIdentitiesService.execute(authenticatedEmail).getBody();
            if (identities == null || identities.getIdentities() == null) {
                return false;
            }

            LinkedHashSet<String> normalizedIdentities = new LinkedHashSet<>();
            for (String identity : identities.getIdentities()) {
                if (identity != null && !identity.isBlank()) {
                    normalizedIdentities.add(normalize(identity));
                }
            }

            return normalizedIdentities.contains(requested);
        } catch (Exception ex) {
            return false;
        }
    }

    private String extractAuthenticatedEmail(Authentication auth) {
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            String claimEmail = jwtAuth.getToken().getClaimAsString("email");
            if (claimEmail != null && !claimEmail.isBlank()) {
                return normalize(claimEmail);
            }
        }

        if (auth.getName() == null || auth.getName().isBlank()) {
            return null;
        }

        return normalize(auth.getName());
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Helper: check if user has a specific role
     */
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role) || authority.equals(role));
    }
}
