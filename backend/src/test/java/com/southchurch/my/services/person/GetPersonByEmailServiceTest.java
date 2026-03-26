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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPersonByEmailServiceTest {

    @Mock
    private PeopleRepository repository;

    @InjectMocks
    private GetPersonByEmailService getPersonByEmailService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Person buildPerson(String firstName, String lastName, String primaryEmail, String secondaryEmail) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Person person = constructor.newInstance();
            person.setId(UUID.randomUUID());
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setPrimaryEmail(primaryEmail);
            person.setSecondaryEmail(secondaryEmail);
            return person;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Person via reflection", e);
        }
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    void returnsPersonWhenFoundByPrimaryEmail() {
        Person person = buildPerson("John", "Doe", "john@test.com", "john.secondary@test.com");

        when(repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("john@test.com", "john@test.com"))
                .thenReturn(Optional.of(person));

        ResponseEntity<PersonResponse> response = getPersonByEmailService.execute("john@test.com");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
        assertEquals("john@test.com", response.getBody().getPrimaryEmail());

        verify(repository).findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("john@test.com", "john@test.com");
    }

    @Test
    void returnsPersonWhenFoundBySecondaryEmail() {
        Person person = buildPerson("Jane", "Smith", "jane@test.com", "jane.secondary@test.com");

        when(repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(
                "jane.secondary@test.com", "jane.secondary@test.com"))
                .thenReturn(Optional.of(person));

        ResponseEntity<PersonResponse> response =
                getPersonByEmailService.execute("jane.secondary@test.com");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Jane", response.getBody().getFirstName());
        assertEquals("Smith", response.getBody().getLastName());
        assertEquals("jane@test.com", response.getBody().getPrimaryEmail());
        assertEquals("jane.secondary@test.com", person.getSecondaryEmail());

        verify(repository).findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(
                "jane.secondary@test.com", "jane.secondary@test.com");
    }

    @Test
    void normalizesEmailBeforeQueryingRepository() {
        Person person = buildPerson("John", "Doe", "john@test.com", null);

        when(repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("john@test.com", "john@test.com"))
                .thenReturn(Optional.of(person));

        ResponseEntity<PersonResponse> response = getPersonByEmailService.execute("  JOHN@TEST.COM  ");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("john@test.com", response.getBody().getPrimaryEmail());

        verify(repository).findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("john@test.com", "john@test.com");
    }

    // -------------------------------------------------------------------------
    // Failure path
    // -------------------------------------------------------------------------

    @Test
    void throwsWhenPersonNotFound() {
        when(repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("missing@test.com", "missing@test.com"))
                .thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class,
                () -> getPersonByEmailService.execute("missing@test.com"));

        verify(repository).findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("missing@test.com", "missing@test.com");
    }

    @Test
    void throwsWhenEmailIsNull() {
        assertThrows(NullPointerException.class, () -> getPersonByEmailService.execute(null));

        verifyNoInteractions(repository);
    }

    @Test
    void throwsWhenEmailIsBlank() {
        when(repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("", ""))
                .thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class,
                () -> getPersonByEmailService.execute("   "));

        verify(repository).findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase("", "");
    }
}