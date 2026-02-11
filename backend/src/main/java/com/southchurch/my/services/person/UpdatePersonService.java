package com.southchurch.my.services.person;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Command;
import com.southchurch.my.dto.UpdatePersonCommand;
import com.southchurch.my.dto.UpdatePersonRequest;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class UpdatePersonService implements Command<UpdatePersonCommand, UpdatePersonRequest>{

    private final PeopleRepository repository;

    public UpdatePersonService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    public ResponseEntity<UpdatePersonRequest> execute(UpdatePersonCommand input) {
    
        Optional<Person> person = repository.findById(input.getId());

        // TODO: implement business logic and validation

        return null;
    }

}
