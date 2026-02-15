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
public class GetPersonByEmailService implements Query<String, PersonResponse> {

    private final PeopleRepository repository;

    public GetPersonByEmailService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "personByEmailCache", key = "#email.trim().toLowerCase()")
    public ResponseEntity<PersonResponse> execute(String email) {

        String normalized = email.trim().toLowerCase();

        Person person = repository.findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(normalized, normalized)
                .orElseThrow(PersonNotFoundException::new);

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(person));
    }

}
