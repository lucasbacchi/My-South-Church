package com.southchurch.my.dto;

import java.time.LocalDate;
import java.util.Set;

import lombok.Data;

@Data
public class UpdatePersonRequest {
    private String firstName;
    private String lastName;
    private String primaryEmail;
    private String secondaryEmail;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String firebaseUID;

    private Set<String> roles; 
}
