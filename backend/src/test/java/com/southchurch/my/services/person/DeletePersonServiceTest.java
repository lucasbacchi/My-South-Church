package com.southchurch.my.services.person;

import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.security.FirebaseCustomClaimsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletePersonServiceTest {

    @Mock
    private PeopleRepository repository;

    @Mock
    private FirebaseCustomClaimsService firebaseClaimsService;

    @InjectMocks
    private DeletePersonService deletePersonService;

    private Person buildPerson(String firebaseUID) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
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
    void deletesPersonSuccessfully() {
        UUID id = UUID.randomUUID();
        Person person = buildPerson(null);

        when(repository.findById(id)).thenReturn(Optional.of(person));

        ResponseEntity<Void> response = deletePersonService.execute(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(repository).deleteById(id);
    }

    @Test
    void clearsFirebaseClaimsWhenFirebaseUidPresent() {
        UUID id = UUID.randomUUID();
        Person person = buildPerson("firebase-uid-123");

        when(repository.findById(id)).thenReturn(Optional.of(person));

        deletePersonService.execute(id);

        verify(firebaseClaimsService).clearRoles("firebase-uid-123");
        verify(repository).deleteById(id);
    }

    @Test
    void doesNotClearFirebaseClaimsWhenFirebaseUidIsNull() {
        UUID id = UUID.randomUUID();
        Person person = buildPerson(null);

        when(repository.findById(id)).thenReturn(Optional.of(person));

        deletePersonService.execute(id);

        verify(firebaseClaimsService, never()).clearRoles(any());
        verify(repository).deleteById(id);
    }

    @Test
    void doesNotClearFirebaseClaimsWhenFirebaseUidIsBlank() {
        UUID id = UUID.randomUUID();
        Person person = buildPerson("  ");

        when(repository.findById(id)).thenReturn(Optional.of(person));

        deletePersonService.execute(id);

        verify(firebaseClaimsService, never()).clearRoles(any());
        verify(repository).deleteById(id);
    }

    // -------------------------------------------------------------------------
    // Not found
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenPersonNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> deletePersonService.execute(id));
        verify(repository, never()).deleteById(any());
    }

    @Test
    void doesNotDeleteWhenPersonNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> deletePersonService.execute(id));
        verify(firebaseClaimsService, never()).clearRoles(any());
    }
}