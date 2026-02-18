package com.southchurch.my.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import com.southchurch.my.services.group.GetGroupsService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

@Component
public class UserAccessValidator implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(UserAccessValidator.class);

    private final GetGroupsService googleService;
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    // Inject the group email from application.properties
    @Value("${google.groups.root-group}")
    private String rootGroupEmail;

    // Cache: email -> { isMember, timestamp }
    private final ConcurrentHashMap<String, GroupCacheEntry> groupCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 20 * 60 * 1000; // 20 minutes
    private static final long REFRESH_BEFORE_EXPIRY_MS = 5 * 60 * 1000; // Refresh 5 minutes before expiry

    private static class GroupCacheEntry {
        boolean isMember;
        long timestamp;

        GroupCacheEntry(boolean isMember) {
            this.isMember = isMember;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_TTL_MS;
        }

        boolean shouldRefresh() {
            return System.currentTimeMillis() - timestamp > (CACHE_TTL_MS - REFRESH_BEFORE_EXPIRY_MS);
        }
    }

    public UserAccessValidator(GetGroupsService googleService) {
        this.googleService = googleService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String userEmail = jwt.getClaimAsString("email");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        if (emailVerified == null || !emailVerified) {
            throw new InvalidUserException("Access Denied: email is not verified for " + userEmail);
        }

        boolean isTrusted;
        try {
            isTrusted = checkGroupMembership(userEmail);
        } catch (IOException e) {
            throw new InvalidUserException("Error verifying group membership: " + e.getMessage(), e);
        }

        if (!isTrusted) {
            throw new InvalidUserException("Access Denied: " + userEmail + " is not a member of " + rootGroupEmail);
        }

        // Get default authorities from JWT standard claims
        Collection<GrantedAuthority> authorities = new HashSet<>(defaultConverter.convert(jwt));

        // Extract custom roles from Firebase custom claims
        @SuppressWarnings("unchecked")
        List<String> customRoles = (List<String>) jwt.getClaims().get("roles");
        if (customRoles != null && !customRoles.isEmpty()) {
            for (String role : customRoles) {
                // Ensure role has ROLE_ prefix for Spring Security
                String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                authorities.add(new SimpleGrantedAuthority(roleWithPrefix));
            }
        }

        return new JwtAuthenticationToken(jwt, authorities, userEmail);
    }

    /**
     * Check group membership with caching and background refresh.
     * Returns cached result if fresh, proactively refreshes if expiring soon.
     */
    private boolean checkGroupMembership(String userEmail) throws IOException {
        GroupCacheEntry cached = groupCache.get(userEmail);

        // Cache hit and not expiring soon
        if (cached != null && !cached.isExpired() && !cached.shouldRefresh()) {
            return cached.isMember;
        }

        // Cache expired or missing - do synchronous check
        if (cached == null || cached.isExpired()) {
            boolean isMember = googleService.isMemberViaCloudIdentity(userEmail, rootGroupEmail);
            groupCache.put(userEmail, new GroupCacheEntry(isMember));
            return isMember;
        }

        // Cache exists but should refresh - return cached result and refresh async
        refreshGroupMembershipAsync(userEmail);
        return cached.isMember;
    }

    /**
     * Asynchronously refresh group membership to keep cache fresh.
     */
    @Async
    private void refreshGroupMembershipAsync(String userEmail) {
        try {
            boolean isMember = googleService.isMemberViaCloudIdentity(userEmail, rootGroupEmail);
            groupCache.put(userEmail, new GroupCacheEntry(isMember));
        } catch (IOException e) {
            logger.warn("[JWT] Background refresh failed for {}: {}", userEmail, e.getMessage());
        }
    }

    public static class InvalidUserException extends RuntimeException {
        public InvalidUserException(String msg) {
            super(msg);
        }

        public InvalidUserException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }
}
