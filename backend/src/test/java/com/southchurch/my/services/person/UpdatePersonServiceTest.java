package com.southchurch.my.services.person;

import com.southchurch.my.dto.person.PersonRequest;
import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.dto.person.UpdatePersonCommand;
import com.southchurch.my.exceptions.PersonNotFoundException;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdatePersonServiceTest {

    @Mock
    private PeopleRepository peopleRepo;

    @Mock
    private RoleRepository roleRepo;

    @Mock
    private FirebaseCustomClaimsService firebaseClaimsService;

    @Mock
    private VerifyGoogleAccountService verifyGoogleAccountService;

    @InjectMocks
    private UpdatePersonService updatePersonService;

    private UUID personId;
    private Person existingPerson;
    private PersonRequest validRequest;

    @BeforeEach
    void setUp() {
        personId = UUID.randomUUID();
        existingPerson = buildPerson(personId, "John", "Doe", "john@test.com", null);

        validRequest = new PersonRequest();
        validRequest.setFirstName("John");
        validRequest.setLastName("Doe");
        validRequest.setPrimaryEmail("john@test.com");
    }

    private Person buildPerson(UUID id, String firstName, String lastName, String email, String firebaseUID) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            person.setId(id);
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setPrimaryEmail(email);
            person.setFirebaseUID(firebaseUID);
            return person;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Person via reflection", e);
        }
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void updatesPersonSuccessfully() {
        validRequest.setFirstName("Jane");

        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<PersonResponse> response = updatePersonService.execute(
                new UpdatePersonCommand(personId, validRequest));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jane", response.getBody().getFirstName());
    }

    @Test
    void updatesRolesAndSyncsToFirebase() {
        validRequest.setFirebaseUID("uid-123");
        validRequest.setRoles(Set.of("ADMIN"));

        existingPerson.setFirebaseUID("uid-123");
        Role adminRole = new Role("ADMIN");

        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(roleRepo.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        updatePersonService.execute(new UpdatePersonCommand(personId, validRequest));

        verify(firebaseClaimsService).syncRolesToFirebase(eq("uid-123"), anyList());
    }

    @Test
    void reverifyGoogleAccountWhenEmailChanges() {
        validRequest.setPrimaryEmail("newemail@test.com");

        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));
        when(verifyGoogleAccountService.verify("newemail@test.com")).thenReturn(true);

        updatePersonService.execute(new UpdatePersonCommand(personId, validRequest));

        verify(verifyGoogleAccountService).verify("newemail@test.com");
        verify(peopleRepo, times(2)).save(any(Person.class));
    }

    @Test
    void doesNotReverifyGoogleAccountWhenEmailUnchanged() {
        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        updatePersonService.execute(new UpdatePersonCommand(personId, validRequest));

        verify(verifyGoogleAccountService, never()).verify(any());
        verify(peopleRepo, times(1)).save(any(Person.class));
    }

    @Test
    void doesNotSyncFirebaseWhenNoFirebaseUid() {
        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        updatePersonService.execute(new UpdatePersonCommand(personId, validRequest));

        verify(firebaseClaimsService, never()).syncRolesToFirebase(any(), any());
    }

    @Test
    void clearsSecondaryEmailWhenNull() {
        existingPerson.setSecondaryEmail("old@test.com");
        validRequest.setSecondaryEmail(null);

        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.of(existingPerson));
        when(peopleRepo.save(any(Person.class))).thenAnswer(i -> i.getArgument(0));

        ResponseEntity<PersonResponse> response = updatePersonService.execute(
                new UpdatePersonCommand(personId, validRequest));

        assertNull(response.getBody().getSecondaryEmail());
    }

    // -------------------------------------------------------------------------
    // Validation failures
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenPersonNotFound() {
        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(peopleRepo.findById(personId)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () ->
                updatePersonService.execute(new UpdatePersonCommand(personId, validRequest)));
    }

    @Test
    void throwsWhenEmailAlreadyTakenByAnotherPerson() {
        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), eq(personId))).thenReturn(true);

        assertThrows(PersonNotValidException.class, () ->
                updatePersonService.execute(new UpdatePersonCommand(personId, validRequest)));
    }

    @Test
    void throwsWhenRoleDoesNotExist() {
        validRequest.setRoles(Set.of("NONEXISTENT_ROLE"));

        when(peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(roleRepo.findByName("NONEXISTENT_ROLE")).thenReturn(Optional.empty());

        assertThrows(PersonNotValidException.class, () ->
                updatePersonService.execute(new UpdatePersonCommand(personId, validRequest)));
    }

    @Test
    void throwsWhenRequestIsNull() {
        assertThrows(PersonNotValidException.class, () ->
                updatePersonService.execute(new UpdatePersonCommand(personId, null)));
    }

    @Test
    void throwsWhenIdIsNull() {
        assertThrows(PersonNotValidException.class, () ->
                updatePersonService.execute(new UpdatePersonCommand(null, validRequest)));
    }
}