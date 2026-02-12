package com.southchurch.my.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import com.southchurch.my.services.group.GetGroupsService;

import java.util.Collection;
import java.io.IOException;

@Component
public class UserAccessValidator implements Converter<Jwt, AbstractAuthenticationToken> {

    private final GetGroupsService googleService;
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    // Inject the group email from application.properties
    @Value("${google.groups.root-group}")
    private String rootGroupEmail;

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
            isTrusted = googleService.isMemberViaCloudIdentity(userEmail, rootGroupEmail);
        } catch (IOException e) {
            throw new InvalidUserException("Error verifying group membership: " + e.getMessage(), e);
        }

        if (!isTrusted) {
            // This logs the rejection and returns a 401/403 to the frontend
            throw new InvalidUserException("Access Denied: " + userEmail + " is not a member of " + rootGroupEmail);
        }

        // If trusted, let them in
        Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities, userEmail);
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
