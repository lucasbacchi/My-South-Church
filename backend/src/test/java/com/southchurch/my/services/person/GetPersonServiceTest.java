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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPersonServiceTest {

    @Mock
    private PeopleRepository repository;

    @InjectMocks
    private GetPersonService getPersonService;

    private Person buildPerson(String firstName, String lastName, String email) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            person.setId(UUID.randomUUID());
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setPrimaryEmail(email);
            return person;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Person via reflection", e);
        }
    }

    @Test
    void returnsPersonById() {
        UUID id = UUID.randomUUID();
        Person person = buildPerson("John", "Doe", "john@test.com");
        person.setId(id);

        when(repository.findById(id)).thenReturn(Optional.of(person));

        ResponseEntity<PersonResponse> response = getPersonService.execute(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("john@test.com", response.getBody().getPrimaryEmail());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("Doe", response.getBody().getLastName());
    }

    @Test
    void throwsWhenPersonNotFound() {
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PersonNotFoundException.class, () -> getPersonService.execute(id));
    }
}