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
    Optional<Person> findByFirebaseUID(String firebaseUID);
}