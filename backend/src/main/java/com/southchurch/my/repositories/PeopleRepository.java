package com.southchurch.my.repositories;

import com.southchurch.my.models.Person;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// This magic interface gives you .save(), .findAll(), .delete(), etc. for free!
@Repository
public interface PeopleRepository extends JpaRepository<Person, UUID> {
    
    boolean existsByPrimaryEmail(String primaryEmail);

    // these methods are using spring data jpa to parse the method name to a sql query
    Optional<Person> findByFirebaseUID(String firebaseUID);

    Optional<Person> findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(String primaryEmail, String secondaryEmail);
}
