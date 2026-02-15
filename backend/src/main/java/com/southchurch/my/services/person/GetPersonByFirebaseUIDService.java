package com.southchurch.my.services.person;

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

    public GetPersonByFirebaseUIDService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "personByFirebaseCache", key = "#uid.trim()")
    public ResponseEntity<PersonResponse> execute(String uid) {

        Person person = repository.findByFirebaseUID(uid)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));

    }

}
