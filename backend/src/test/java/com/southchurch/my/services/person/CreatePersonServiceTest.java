package com.southchurch.my.services.person;

import com.southchurch.my.dto.person.PersonRequest;
import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.exceptions.PersonNotValidException;
import com.southchurch.my.models.Person;
import com.southchurch.my.models.Role;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.repositories.RoleRepository;
import com.southchurch.my.security.FirebaseCustomClaimsService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreatePersonServiceTest {

    @Mock
    private PeopleRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private FirebaseCustomClaimsService firebaseClaimsService;

    @Mock
    private VerifyGoogleAccountService verifyGoogleAccountService;

    @InjectMocks
    private CreatePersonService createPersonService;

    private PersonRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new PersonRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setPrimaryEmail("john.doe@test.com");
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void createsPersonSuccessfully() {
        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify(any())).thenReturn(true);

        ResponseEntity<PersonResponse> response = createPersonService.execute(validRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john.doe@test.com", response.getBody().getPrimaryEmail());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        verify(repository, times(2)).save(any(Person.class)); // once for create, once for verify
    }

    @Test
    void setsGoogleAccountVerifiedOnCreation() {
        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify(any())).thenReturn(true);

        ResponseEntity<PersonResponse> response = createPersonService.execute(validRequest);

        assertTrue(response.getBody().getGoogleAccountVerified());
    }

    @Test
    void syncsRolesToFirebaseWhenFirebaseUidPresent() {
        validRequest.setFirebaseUID("firebase-uid-123");
        validRequest.setRoles(Set.of("ADMIN"));

        Role adminRole = new Role("ADMIN");

        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify(any())).thenReturn(true);

        createPersonService.execute(validRequest);

        verify(firebaseClaimsService).syncRolesToFirebase(eq("firebase-uid-123"), anyList());
    }

    @Test
    void doesNotSyncFirebaseWhenNoFirebaseUid() {
        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify(any())).thenReturn(true);

        createPersonService.execute(validRequest);

        verify(firebaseClaimsService, never()).syncRolesToFirebase(any(), any());
    }

    @Test
    void assignsRoleToPersonWhenRoleExists() {
        validRequest.setRoles(Set.of("ADMIN"));

        Role adminRole = new Role("ADMIN");

        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(repository.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify(any())).thenReturn(true);

        ResponseEntity<PersonResponse> response = createPersonService.execute(validRequest);

        assertTrue(response.getBody().getRoles().contains("ADMIN"));
    }

    // -------------------------------------------------------------------------
    // Validation failures
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenRequestIsNull() {
        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(null));
    }

    @Test
    void throwsWhenFirstNameIsBlank() {
        validRequest.setFirstName("  ");
        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }

    @Test
    void throwsWhenLastNameIsBlank() {
        validRequest.setLastName("");
        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }

    @Test
    void throwsWhenPrimaryEmailIsBlank() {
        validRequest.setPrimaryEmail(null);
        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }

    @Test
    void throwsWhenPrimaryEmailAlreadyExists() {
        when(repository.existsByPrimaryEmail(any())).thenReturn(true);
        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }

    @Test
    void throwsWhenFirebaseUidAlreadyExists() {
        validRequest.setFirebaseUID("existing-uid");

        when(repository.existsByPrimaryEmail(any())).thenReturn(false);

        Person existingPerson = mock(Person.class);
        when(repository.findByFirebaseUID("existing-uid"))
            .thenReturn(Optional.of(existingPerson));

        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }

    @Test
    void throwsWhenRoleDoesNotExist() {
        validRequest.setRoles(Set.of("NONEXISTENT_ROLE"));

        when(repository.existsByPrimaryEmail(any())).thenReturn(false);
        when(roleRepository.findByName("NONEXISTENT_ROLE")).thenReturn(Optional.empty());

        assertThrows(PersonNotValidException.class, () -> createPersonService.execute(validRequest));
    }
}