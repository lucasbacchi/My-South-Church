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
import com.southchurch.my.exceptions.people.PersonNotFoundException;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.security.FirebaseCustomClaimsService;

@Service
public class DeletePersonService implements Command<UUID, Void> {

    private final PeopleRepository repository;
    private final FirebaseCustomClaimsService firebaseClaimsService;

    public DeletePersonService(PeopleRepository repository, FirebaseCustomClaimsService firebaseClaimsService) {
        this.repository = repository;
        this.firebaseClaimsService = firebaseClaimsService;
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

        if (person.isPresent()) {
            Person p = person.get();

            // Clear Firebase custom claims if person has Firebase UID
            if (p.getFirebaseUID() != null && !p.getFirebaseUID().isEmpty()) {
                firebaseClaimsService.clearRoles(p.getFirebaseUID());
            }

            repository.deleteById(id);

            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        throw new PersonNotFoundException();
    }

}
