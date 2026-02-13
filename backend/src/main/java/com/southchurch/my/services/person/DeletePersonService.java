package com.southchurch.my.services.person;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.Command;
import com.southchurch.my.exceptions.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

@Service
public class DeletePersonService implements Command<UUID, Void>{

    private final PeopleRepository repository;

    public DeletePersonService(PeopleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "peopleAllCache", allEntries = true),
        @CacheEvict(value = "personByEmailCache", allEntries = true),
        @CacheEvict(value = "personByFirebaseCache", allEntries = true),
        @CacheEvict(value = "personByIdCache", key = "#id")
    })
    public ResponseEntity<Void> execute(UUID id) {

        Optional<Person> person = repository.findById(id);

        if(person.isPresent()) {
            repository.deleteById(id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        throw new PersonNotFoundException();
    }

}
