package com.southchurch.my.services.person;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.exceptions.people.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPersonByFirebaseUIDService implements Query<String, PersonResponse> {

    private final PeopleRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GetPersonByFirebaseUIDService.class);

    public GetPersonByFirebaseUIDService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "personByFirebaseCache", key = "#uid.trim()")
    public ResponseEntity<PersonResponse> execute(String uid) {

        logger.info("Executing " + getClass() + " input : " + uid);

        Person person = repository.findByFirebaseUID(uid)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));

    }

}
