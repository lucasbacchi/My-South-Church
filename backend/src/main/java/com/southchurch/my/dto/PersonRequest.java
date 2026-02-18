package com.southchurch.my.dto;

import java.time.LocalDate;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

// Request DTO (mutable)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
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
