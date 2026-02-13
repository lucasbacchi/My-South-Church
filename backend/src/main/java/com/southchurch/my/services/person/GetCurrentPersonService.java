package com.southchurch.my.services.person;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.exceptions.PersonNotFoundException;

@Service
public class GetCurrentPersonService implements Query<JwtAuthenticationToken, PersonResponse> {

    private final GetPersonByFirebaseUIDService getPersonByFirebaseUIDService;
    private final GetPersonByEmailService getPersonByEmailService;

    public GetCurrentPersonService(GetPersonByFirebaseUIDService getPersonByFirebaseUIDService,
            GetPersonByEmailService getPersonByEmailService) {
        this.getPersonByFirebaseUIDService = getPersonByFirebaseUIDService;
        this.getPersonByEmailService = getPersonByEmailService;
    }

    @Override
    public ResponseEntity<PersonResponse> execute(JwtAuthenticationToken authentication) {

        // Get UID from token claims
        String uid = authentication.getToken().getClaimAsString("user_id");
        if (uid == null || uid.isBlank()) {
            uid = authentication.getToken().getSubject();
        }

        // First try to find by Firebase UID, then fall back to email if UID is missing
        if (uid != null && !uid.isBlank()) {
            return getPersonByFirebaseUIDService.execute(uid);
        }

        // Get email from token claims and try to find by email
        String email = authentication.getToken().getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return getPersonByEmailService.execute(email);
        }

        // If we can't find the user by either UID or email, throw an exception
        throw new PersonNotFoundException();
    }

}
