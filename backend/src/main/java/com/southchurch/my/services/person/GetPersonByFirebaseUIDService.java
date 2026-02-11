package com.southchurch.my.services.person;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPersonByFirebaseUIDService implements Query<String, PersonResponse> {

    private final PeopleRepository repository;

    public GetPersonByFirebaseUIDService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<PersonResponse> execute(String uid) {

        Optional<Person> person = repository.findByFirebaseUID(uid);

        if(person.isPresent()){
            return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person.get()));
        }

        throw new RuntimeException("Person not found");
    }

}
