package com.southchurch.my.services.person;

import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPeopleServiceTest {

    @Mock
    private PeopleRepository repository;

    @InjectMocks
    private GetPeopleService getPeopleService;

    private Person buildPerson(String firstName, String lastName, String email) {
        try {
            java.lang.reflect.Constructor<Person> constructor = Person.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Person person = constructor.newInstance();
            person.setFirstName(firstName);
            person.setLastName(lastName);
            person.setPrimaryEmail(email);
            return person;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Person via reflection", e);
        }
    }

    @Test
    void returnsAllPeople() {
        Person p1 = buildPerson("John", "Doe", "john@test.com");
        Person p2 = buildPerson("Jane", "Smith", "jane@test.com");

        when(repository.findAllWithRoles()).thenReturn(List.of(p1, p2));

        ResponseEntity<List<PersonResponse>> response = getPeopleService.execute(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("john@test.com", response.getBody().get(0).getPrimaryEmail());
        assertEquals("jane@test.com", response.getBody().get(1).getPrimaryEmail());
    }

    @Test
    void returnsEmptyListWhenNoPeople() {
        when(repository.findAllWithRoles()).thenReturn(List.of());

        ResponseEntity<List<PersonResponse>> response = getPeopleService.execute(null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void mapsPersonToPersonResponse() {
        Person person = buildPerson("John", "Doe", "john@test.com");

        when(repository.findAllWithRoles()).thenReturn(List.of(person));

        ResponseEntity<List<PersonResponse>> response = getPeopleService.execute(null);

        PersonResponse pr = response.getBody().get(0);
        assertEquals("John", pr.getFirstName());
        assertEquals("Doe", pr.getLastName());
        assertEquals("john@test.com", pr.getPrimaryEmail());
    }
}