package com.southchurch.my.services.person;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.southchurch.my.Query;
import com.southchurch.my.dto.person.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class GetPeopleService implements Query<Void, List<PersonResponse>> {

    private final PeopleRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GetPeopleService.class);

    public GetPeopleService(PeopleRepository repository) {
        this.repository = repository;
    }

    /**
     * Executes a database call to fetch all Person.
     * 
     * @return a ResponseEntity containing the fetched people, or an error if the
     *         call fails
     */
    @Override
    @Cacheable(value = "peopleAllCache")
    public ResponseEntity<List<PersonResponse>> execute(Void input) {

        logger.info("Executing " + getClass() + " input : " + input);

        List<Person> people = repository.findAllWithRoles();

        List<PersonResponse> peopleList = people.stream().map(PersonResponse::new).toList();

        return ResponseEntity.status(HttpStatus.OK).body(peopleList);

    }

}
