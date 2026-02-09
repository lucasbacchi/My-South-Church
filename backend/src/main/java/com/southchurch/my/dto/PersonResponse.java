package com.southchurch.my.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.southchurch.my.models.Person;
import com.southchurch.my.models.Role;

import lombok.Data;

@Data
public class PersonResponse {

    public PersonResponse(Person person) {
        this.id = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.primaryEmail = person.getPrimaryEmail();
        this.secondaryEmail = person.getSecondaryEmail();
        this.phoneNumber = person.getPhoneNumber();
        this.dateOfBirth = person.getDateOfBirth();
        this.firebaseUID = person.getFirebaseUID();
        this.lastLogin = person.getLastLogin();

        this.roles = person.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private UUID id;
    private String firstName;
    private String lastName;
    private String primaryEmail;
    private String secondaryEmail;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String firebaseUID;
    private LocalDateTime lastLogin;

    private Set<String> roles;
}
