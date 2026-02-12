package com.southchurch.my.services.person;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPersonService implements Query<UUID, PersonResponse> {

    private final PeopleRepository repository;

    public GetPersonService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<PersonResponse> execute(UUID id) {
        
        Optional<Person> person = repository.findById(id);
        
        if(person.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person.get()));

        throw new RuntimeException("Person not found");
    }

}
