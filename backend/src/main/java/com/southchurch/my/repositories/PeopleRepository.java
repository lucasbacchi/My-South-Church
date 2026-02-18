package com.southchurch.my.repositories;

import com.southchurch.my.models.Person;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleRepository extends JpaRepository<Person, UUID> {

    boolean existsByPrimaryEmail(String primaryEmail);

    // these methods are using spring data jpa to parse the method name to a sql
    // query
    Optional<Person> findByFirebaseUID(String firebaseUID);

    Optional<Person> findByPrimaryEmailIgnoreCaseOrSecondaryEmailIgnoreCase(String primaryEmail, String secondaryEmail);

    boolean existsByPrimaryEmailIgnoreCaseAndIdNot(String primaryEmail, UUID id);

    boolean existsByFirebaseUIDAndIdNot(String firebaseUID, UUID id);

    @Query("SELECT DISTINCT p FROM Person p LEFT JOIN FETCH p.roles ORDER BY p.lastName ASC, p.firstName ASC")
    List<Person> findAllWithRoles();
}
