package com.southchurch.my.services.person;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.exceptions.people.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetCurrentPersonService implements Query<JwtAuthenticationToken, PersonResponse> {

    private final GetPersonByFirebaseUIDService getPersonByFirebaseUIDService;
    private final GetPersonByEmailService getPersonByEmailService;
    private final PeopleRepository repository;

    public GetCurrentPersonService(GetPersonByFirebaseUIDService getPersonByFirebaseUIDService,
            GetPersonByEmailService getPersonByEmailService,
            PeopleRepository repository) {
        this.getPersonByFirebaseUIDService = getPersonByFirebaseUIDService;
        this.getPersonByEmailService = getPersonByEmailService;
        this.repository = repository;
    }

    @Override
    @Caching(evict = @CacheEvict(value = "peopleAllCache", allEntries = true), put = {
            @CachePut(value = "personByIdCache", key = "#result.body.id", condition = "#result?.body?.id != null"),
            @CachePut(value = "personByEmailCache", key = "#result.body.primaryEmail.trim().toLowerCase()", condition = "#result?.body?.primaryEmail != null"),
            @CachePut(value = "personByFirebaseCache", key = "#result.body.firebaseUID.trim()", condition = "#result?.body?.firebaseUID != null")
    })
    public ResponseEntity<PersonResponse> execute(JwtAuthenticationToken authentication) {

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
        Person updatedPerson = repository.save(person);
        PersonResponse updatedResponse = new PersonResponse(updatedPerson);

        return ResponseEntity.ok(updatedResponse);
    }

}
