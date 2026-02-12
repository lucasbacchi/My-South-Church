package com.southchurch.my.exceptions;

public enum ErrorMessages {

    INVALID_ID("Invalid ID"),
    PERSON_NOT_FOUND("Person not found"),

    REQUEST_BODY_REQUIRED("Request body is required"),

    FIRST_NAME_REQUIRED("First Name is required"),
    LAST_NAME_REQUIRED("Last Name is required"),
    PRIMARY_EMAIL_REQUIRED("Primary Email is required"),

    PRIMARY_EMAIL_ALREADY_EXISTS("Primary Email already exists"),
    FIREBASE_UID_ALREADY_EXISTS("FirebaseUID already exists"),

    INVALID_ROLE("Invalid Role");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
