package com.southchurch.my.services.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPersonByEmailService implements Query<String, PersonResponse> {

    private final PeopleRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GetPersonByEmailService.class);

    public GetPersonByEmailService(PeopleRepository repository) {
        this.repository = repository;
    }

    /**
     * Executes a query to fetch a person by their email address.
     * 
     * @param email the email address of the person to fetch
     * @return a ResponseEntity containing the fetched person, or an error if the call fails
     * @throws PersonNotFoundException if the person does not exist in the database
     */
    @Override
    @Cacheable(value = "personByEmailCache", key = "#email.trim().toLowerCase()")
    public ResponseEntity<PersonResponse> execute(String email) {

        logger.info("Executing " + getClass() + " input : " + email);

        String normalized = email.trim().toLowerCase();

        Person person = repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(normalized, normalized)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));
    }

}
