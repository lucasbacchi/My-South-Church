package com.southchurch.my.validators;

import com.southchurch.my.dto.CreatePersonRequest;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.repositories.PeopleRepository;

public class PersonValidator {

    public PersonValidator() {
    }

    public static void execute(CreatePersonRequest input, PeopleRepository repository) {

        if (input == null) {
            throw new RuntimeException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        if (isBlank(input.getFirstName())) {
            throw new RuntimeException(ErrorMessages.FIRST_NAME_REQUIRED.getMessage());
        }

        if (isBlank(input.getLastName())) {
            throw new RuntimeException(ErrorMessages.LAST_NAME_REQUIRED.getMessage());
        }

        if (isBlank(input.getPrimaryEmail())) {
            throw new RuntimeException(ErrorMessages.PRIMARY_EMAIL_REQUIRED.getMessage());
        }

        String email = input.getPrimaryEmail().trim().toLowerCase();
        if (repository.existsByPrimaryEmail(email)) {
            throw new RuntimeException(ErrorMessages.PRIMARY_EMAIL_ALREADY_EXISTS.getMessage());
        }

        if (!isBlank(input.getFirebaseUID())) {
            String uid = input.getFirebaseUID().trim();
            if (repository.findByFirebaseUID(uid).isPresent()) {
                throw new RuntimeException(ErrorMessages.FIREBASE_UID_ALREADY_EXISTS.getMessage());
            }
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
