package com.southchurch.my.models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;
    private String primaryEmail;
    private String secondaryEmail;
    private String phoneNumber;
    private String photoUrl;
}