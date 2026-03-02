package com.southchurch.my.services.person;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPersonService implements Query<UUID, PersonResponse> {

    private final PeopleRepository repository;

    private static final Logger logger = LoggerFactory.getLogger(GetPersonService.class);

    public GetPersonService(PeopleRepository repository) {
        this.repository = repository;
    }

    /**
     * Executes a query to fetch a Person by their UUID.
     * 
     * @param id The UUID of the person to fetch.
     * @return A ResponseEntity containing a PersonResponse or null if the user was
     *         not found.
     * @throws PersonNotFoundException if the user was not found.
     */
    @Override
    @Cacheable(value = "personByIdCache", key = "#id")
    public ResponseEntity<PersonResponse> execute(UUID id) {

        logger.info("Executing " + getClass() + " input : " + id);

        Person person = repository.findById(id)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));
    }

}
