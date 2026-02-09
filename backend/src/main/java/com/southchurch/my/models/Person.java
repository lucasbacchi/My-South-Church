package com.southchurch.my.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.southchurch.my.dto.CreatePersonRequest;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "peoplev2")
public class Person {

    protected Person() {}

    public Person(CreatePersonRequest input) {
        this.firstName = input.getFirstName().trim();
        this.lastName = input.getLastName().trim();
        this.primaryEmail = input.getPrimaryEmail().trim().toLowerCase();
        this.secondaryEmail = input.getSecondaryEmail() == null ? null : input.getSecondaryEmail().trim();
        this.phoneNumber = input.getPhoneNumber() == null ? null : input.getPhoneNumber().trim();
        this.dateOfBirth = input.getDateOfBirth();
        this.firebaseUID = input.getFirebaseUID() == null ? null : input.getFirebaseUID().trim();
    }

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "firstName", nullable = false, length = 128)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 128)
    private String lastName;

    @Column(name = "primaryEmail", nullable = false, length = 128, unique = true)
    private String primaryEmail;

    @Column(name = "secondaryEmail", length = 128)
    private String secondaryEmail;

    @Column(name = "lastLogin")
    private LocalDateTime lastLogin;

    @Column(name = "phoneNumber", length = 45)
    private String phoneNumber;

    @Column(name = "dateOfBirth")
    private LocalDate dateOfBirth;

    @Column(name = "firebaseUID", length = 128, unique = true)
    private String firebaseUID;

    @PrePersist
    private void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }
}