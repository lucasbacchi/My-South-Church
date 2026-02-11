package com.southchurch.my.validators;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.models.Role;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.repositories.RoleRepository;

public class PersonValidator {

    public PersonValidator() {
    }

    public static void validateCreate(
        PersonRequest req, 
        PeopleRepository peopleRepo,
        RoleRepository roleRepo
    ){

        if (req == null) {
            throw new RuntimeException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
        }

        // These are required for creating a person
        requireNotBlank(req.getFirstName(), ErrorMessages.FIRST_NAME_REQUIRED.getMessage());
        requireNotBlank(req.getLastName(), ErrorMessages.LAST_NAME_REQUIRED.getMessage());
        requireNotBlank(req.getPrimaryEmail(), ErrorMessages.PRIMARY_EMAIL_REQUIRED.getMessage());

        // Validate and Uniqueness
        validateEmailUniquenessForCreate(req, peopleRepo);
        validateFirebaseUidUniquenessForCreate(req, peopleRepo);

        // roles validation
        validateRolesExist(req.getRoles(), roleRepo);
    }

    public static void validateUpdate(
        UUID id,
        PersonRequest req,
        PeopleRepository peopleRepo,
        RoleRepository roleRepo
    ){
        if(id == null) throw new RuntimeException(ErrorMessages.INVALID_ID.getMessage());
        if(req == null) throw new RuntimeException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());

        //only validate fields that are provided (partial update)
        if (req.getFirstName() != null) 
            requireNotBlank(req.getFirstName(), ErrorMessages.FIRST_NAME_REQUIRED.getMessage());
        if (req.getLastName() != null) 
            requireNotBlank(req.getLastName(), ErrorMessages.LAST_NAME_REQUIRED.getMessage());

        if (req.getPrimaryEmail() != null) {
            requireNotBlank(req.getPrimaryEmail(), ErrorMessages.PRIMARY_EMAIL_REQUIRED.getMessage());
            String email = normalizeEmail(req.getPrimaryEmail());
            if (peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(email, id)) {
                throw new RuntimeException(ErrorMessages.PRIMARY_EMAIL_ALREADY_EXISTS.getMessage());
            }
        }

        if (req.getFirebaseUID() != null) {
            String uid = req.getFirebaseUID().trim();
            // allow clearing by ""
            if (!uid.isEmpty() && peopleRepo.existsByFirebaseUIDAndIdNot(uid, id)) {
                throw new RuntimeException(ErrorMessages.FIREBASE_UID_ALREADY_EXISTS.getMessage());
            }
        }

        // roles: if provided, validate each exists
        if (req.getRoles() != null) {
            validateRolesExist(req.getRoles(), roleRepo);
        }
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static void validateFirebaseUidUniquenessForCreate(PersonRequest req, PeopleRepository peopleRepo) {
        if (!isBlank(req.getFirebaseUID())) {
            String uid = req.getFirebaseUID().trim();
            if (peopleRepo.findByFirebaseUID(uid).isPresent()) {
                throw new RuntimeException(ErrorMessages.FIREBASE_UID_ALREADY_EXISTS.getMessage());
            }
        }
    }

    private static void validateRolesExist(Set<String> roles, RoleRepository roleRepo) {

        if(roles == null) return;

        for (String role : roles) {

            if (isBlank(role)) throw new RuntimeException(ErrorMessages.INVALID_ROLE.getMessage());

            String roleName = role.trim().toUpperCase();

            Optional<Role> roleOpt = roleRepo.findByName(roleName);

            if (!roleOpt.isPresent()) {
                throw new RuntimeException(ErrorMessages.INVALID_ROLE.getMessage());
            }
        }
    }

    private static void validateEmailUniquenessForCreate(PersonRequest req, PeopleRepository peopleRepo) {
        String email = normalizeEmail(req.getPrimaryEmail());
        if (peopleRepo.existsByPrimaryEmail(email)) {
            throw new RuntimeException(ErrorMessages.PRIMARY_EMAIL_ALREADY_EXISTS.getMessage());
        }
    }

    private static void requireNotBlank(String field, String message) {
        if (isBlank(field)) {
            throw new RuntimeException(message);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
