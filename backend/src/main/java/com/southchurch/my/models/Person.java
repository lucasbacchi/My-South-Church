package com.southchurch.my.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.southchurch.my.dto.PersonRequest;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
@ToString(exclude = "roles")
@EqualsAndHashCode(exclude = "roles")
@Table(name = "peoplev2")
public class Person {

    protected Person() {
    }

    public Person(PersonRequest input) {
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "peoplev2_roles", joinColumns = @JoinColumn(name = "person_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    @PrePersist
    private void prePersist() {
        if (id == null)
            id = UUID.randomUUID();
    }
}
