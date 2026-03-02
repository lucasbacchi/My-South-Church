package com.southchurch.my.services.person;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetCurrentPersonService implements Query<JwtAuthenticationToken, PersonResponse> {

    private final GetPersonByFirebaseUIDService getPersonByFirebaseUIDService;
    private final GetPersonByEmailService getPersonByEmailService;
    private final PeopleRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GetCurrentPersonService.class);

    public GetCurrentPersonService(GetPersonByFirebaseUIDService getPersonByFirebaseUIDService,
            GetPersonByEmailService getPersonByEmailService,
            PeopleRepository repository) {
        this.getPersonByFirebaseUIDService = getPersonByFirebaseUIDService;
        this.getPersonByEmailService = getPersonByEmailService;
        this.repository = repository;
    }

    /**
     * Executes a query to get the current logged in Person by a JWT authentication
     * token.
     * 
     * @param authentication the JWT authentication token.
     * @return a ResponseEntity containing a PersonResponse or null if the user was
     *         not found.
     * @throws PersonNotFoundException if the user was not found.
     */
    @Override
    @Caching(evict = @CacheEvict(value = "peopleAllCache", allEntries = true), put = {
            @CachePut(value = "personByIdCache", key = "#result.body.id", condition = "#result?.body?.id != null"),
            @CachePut(value = "personByEmailCache", key = "#result.body.primaryEmail.trim().toLowerCase()", condition = "#result?.body?.primaryEmail != null"),
            @CachePut(value = "personByFirebaseCache", key = "#result.body.firebaseUID.trim()", condition = "#result?.body?.firebaseUID != null")
    })
    public ResponseEntity<PersonResponse> execute(JwtAuthenticationToken authentication) {

        logger.info("Executing " + getClass() + " input : " + authentication);

        // Get UID from token claims
        String uid = authentication.getToken().getClaimAsString("user_id");
        if (uid == null || uid.isBlank()) {
            uid = authentication.getToken().getSubject();
        }

        ResponseEntity<PersonResponse> response = null;

        // First try to find by Firebase UID, then fall back to email if UID is missing
        if (uid != null && !uid.isBlank()) {
            response = getPersonByFirebaseUIDService.execute(uid);
        }

        if (response == null || response.getBody() == null) {
            // Get email from token claims and try to find by email
            String email = authentication.getToken().getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                response = getPersonByEmailService.execute(email);
            }
        }

        // If we can't find the user by either UID or email, throw an exception
        if (response == null || response.getBody() == null) {
            throw new PersonNotFoundException();
        }

        // Update lastLogin timestamp
        PersonResponse personResponse = response.getBody();
        Person person = repository.findById(personResponse.getId())
                .orElseThrow(PersonNotFoundException::new);
        person.setLastLogin(LocalDateTime.now());

        // Mark as verified if user logged in with Google
        try {
            Object firebaseClaim = authentication.getToken().getClaim("firebase");
            if (firebaseClaim instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> firebaseMap = (java.util.Map<String, Object>) firebaseClaim;
                Object signInProvider = firebaseMap.get("sign_in_provider");
                if ("google.com".equals(signInProvider)) {
                    person.setGoogleAccountVerified(true);
                }
            }
        } catch (Exception e) {
            // Ignore errors in checking Firebase claims
        }

        Person updatedPerson = repository.save(person);
        PersonResponse updatedResponse = new PersonResponse(updatedPerson);

        return ResponseEntity.ok(updatedResponse);
    }

}
