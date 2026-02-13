package com.southchurch.my.services.person;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPeopleService implements Query<Void, List<PersonResponse>> {

    private final PeopleRepository repository;

    public GetPeopleService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Cacheable(value = "peopleAllCache")
    public ResponseEntity<List<PersonResponse>> execute(Void input) {

        List<Person> people = repository.findAll();

        List<PersonResponse> peopleList = people.stream().map(PersonResponse::new).toList();

        return ResponseEntity.status(HttpStatus.OK).body(peopleList);

    }

}
