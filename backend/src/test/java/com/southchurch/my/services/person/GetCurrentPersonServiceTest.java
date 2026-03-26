package com.southchurch.my.services.person;

import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCurrentPersonServiceTest {

    @Mock
    private GetPersonByFirebaseUIDService getPersonByFirebaseUIDService;

    @Mock
    private GetPersonByEmailService getPersonByEmailService;

    @Mock
    private PeopleRepository repository;

    @InjectMocks
    private GetCurrentPersonService getCurrentPersonService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Person buildPerson(String firstName, String lastName, String email, String firebaseUID) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            person.setId(UUID.randomUUID());
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setPrimaryEmail(email);
            person.setFirebaseUID(firebaseUID);
            return person;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Person via reflection", e);
        }
    }

    private JwtAuthenticationToken buildToken(String userId, String email, Map<String, Object> extraClaims) {
        Jwt.Builder builder = Jwt.withTokenValue("test-token")
                .header("alg", "RS256")
                .subject(userId)
                .claim("email", email);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        Jwt jwt = builder.build();
        return new JwtAuthenticationToken(jwt);
    }

    // -------------------------------------------------------------------------
    // Happy path — found by Firebase UID
    // -------------------------------------------------------------------------

    @Test
    void returnsPersonFoundByFirebaseUid() {
        Person person = buildPerson("John", "Doe", "john@test.com", "uid-123");
        PersonResponse personResponse = new PersonResponse(person);
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com", null);

        when(getPersonByFirebaseUIDService.execute("uid-123")).thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<PersonResponse> response = getCurrentPersonService.execute(token);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("john@test.com", response.getBody().getPrimaryEmail());
        verify(getPersonByFirebaseUIDService).execute("uid-123");
        verify(getPersonByEmailService, never()).execute(any());
    }

    @Test
    void updatesLastLoginOnSuccess() {
        Person person = buildPerson("John", "Doe", "john@test.com", "uid-123");
        PersonResponse personResponse = new PersonResponse(person);
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com", null);

        when(getPersonByFirebaseUIDService.execute("uid-123")).thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        getCurrentPersonService.execute(token);

        verify(repository).save(argThat(p -> p.getLastLogin() != null));
    }

    // -------------------------------------------------------------------------
    // Happy path — falls back to email
    // -------------------------------------------------------------------------

    @Test
    void fallsBackToEmailWhenFirebaseUidNotFound() {
        Person person = buildPerson("John", "Doe", "john@test.com", null);
        PersonResponse personResponse = new PersonResponse(person);
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com", null);

        when(getPersonByFirebaseUIDService.execute("uid-123"))
                .thenReturn(ResponseEntity.ok(null));
        when(getPersonByEmailService.execute("john@test.com"))
                .thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<PersonResponse> response = getCurrentPersonService.execute(token);

        assertEquals(200, response.getStatusCode().value());
        verify(getPersonByEmailService).execute("john@test.com");
    }

    // -------------------------------------------------------------------------
    // Google sign-in provider
    // -------------------------------------------------------------------------

    @Test
    void setsGoogleAccountVerifiedWhenSignedInWithGoogle() {
        Person person = buildPerson("John", "Doe", "john@test.com", "uid-123");
        PersonResponse personResponse = new PersonResponse(person);

        Map<String, Object> firebaseClaim = Map.of("sign_in_provider", "google.com");
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com",
                Map.of("firebase", firebaseClaim));

        when(getPersonByFirebaseUIDService.execute("uid-123")).thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        getCurrentPersonService.execute(token);

        verify(repository).save(argThat(p -> Boolean.TRUE.equals(p.getGoogleAccountVerified())));
    }

    @Test
    void doesNotSetGoogleVerifiedWhenSignedInWithOtherProvider() {
        Person person = buildPerson("John", "Doe", "john@test.com", "uid-123");
        PersonResponse personResponse = new PersonResponse(person);

        Map<String, Object> firebaseClaim = Map.of("sign_in_provider", "password");
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com",
                Map.of("firebase", firebaseClaim));

        when(getPersonByFirebaseUIDService.execute("uid-123")).thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.of(person));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        getCurrentPersonService.execute(token);

        verify(repository).save(argThat(p -> !Boolean.TRUE.equals(p.getGoogleAccountVerified())));
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenNotFoundByUidOrEmail() {
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com", null);

        when(getPersonByFirebaseUIDService.execute("uid-123"))
                .thenReturn(ResponseEntity.ok(null));
        when(getPersonByEmailService.execute("john@test.com"))
                .thenReturn(ResponseEntity.ok(null));

        assertThrows(PersonNotFoundException.class, () -> getCurrentPersonService.execute(token));
    }

    @Test
    void throwsWhenPersonNotFoundInRepoAfterLookup() {
        Person person = buildPerson("John", "Doe", "john@test.com", "uid-123");
        PersonResponse personResponse = new PersonResponse(person);
        JwtAuthenticationToken token = buildToken("uid-123", "john@test.com", null);

        when(getPersonByFirebaseUIDService.execute("uid-123")).thenReturn(ResponseEntity.ok(personResponse));
        when(repository.findById(personResponse.getId())).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> getCurrentPersonService.execute(token));
    }
}