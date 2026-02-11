package com.southchurch.my.dto;

import java.time.LocalDate;
import java.util.Set;

import lombok.Data;

// Request DTO (mutable)
@Data
public class PersonRequest {
    private String firstName;
    private String lastName;
    private String primaryEmail;
    private String secondaryEmail;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String firebaseUID;
    private Set<String> roles; // e.g. ["SUPER_ADMIN", "ADMIN", "USER"]
}
