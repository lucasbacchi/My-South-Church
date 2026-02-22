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
public class GetPersonByFirebaseUIDService implements Query<String, PersonResponse> {

    private final PeopleRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GetPersonByFirebaseUIDService.class);

    public GetPersonByFirebaseUIDService(PeopleRepository repository) {
        this.repository = repository;
    }

    /**
     * Executes a query to get a Person by their Firebase UID.
     * 
     * @param uid The Firebase UID of the person to retrieve.
     * @return A ResponseEntity containing a PersonResponse or null if the user was
     *         not found.
     * @throws PersonNotFoundException if the user was not found.
     */
    @Override
    @Cacheable(value = "personByFirebaseCache", key = "#uid.trim()")
    public ResponseEntity<PersonResponse> execute(String uid) {

        logger.info("Executing " + getClass() + " input : " + uid);

        Person person = repository.findByFirebaseUID(uid)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));

    }

}
