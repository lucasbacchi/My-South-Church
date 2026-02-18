package com.southchurch.my.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Authorization service for endpoint-level access control.
 * Used with @PreAuthorize annotations via SpEL.
 *
 * Example: @PreAuthorize("@authorizationService.canDeletePerson(authentication)")
 */
@Service
public class AuthorizationService {

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
     * Helper: check if user has a specific role
     */
    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role) || authority.equals(role));
    }
}
