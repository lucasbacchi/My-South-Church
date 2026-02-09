package com.southchurch.my.services.person;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.Command;
import com.southchurch.my.dto.CreatePersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.validators.PersonValidator;

@Service
public class CreatePersonService implements Command<CreatePersonRequest, PersonResponse> {

    private final PeopleRepository repository;

    public CreatePersonService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ResponseEntity<PersonResponse> execute(CreatePersonRequest input) {
       
        PersonValidator.execute(input, repository);

        Person person = new Person(input);
        
        Person savedPerson = repository.save(person);

        return ResponseEntity.status(HttpStatus.CREATED).body(new PersonResponse(savedPerson));
    }

}
