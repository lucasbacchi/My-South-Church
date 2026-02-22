package com.southchurch.my.validators;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.PersonNotValidException;
import com.southchurch.my.models.Role;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.repositories.RoleRepository;

public class PersonValidator {

    private static final Logger logger = LoggerFactory.getLogger(PersonValidator.class);

    public PersonValidator() {
    }

    /**
     * Validates a PersonRequest for creating a new Person in the database.
     * 
     * @param req        the PersonRequest to validate
     * @param peopleRepo the PeopleRepository to check for uniqueness
     * @param roleRepo   the RoleRepository to check for role existence
     * @throws PersonNotValidException if the PersonRequest is null, or if any of
     *                                 the required fields are blank,
     *                                 or if the primary email address is not
     *                                 unique, or if the Firebase UID is not unique,
     *                                 or if any of the roles are invalid.
     */
    public static void validateCreate(
            PersonRequest req,
            PeopleRepository peopleRepo,
            RoleRepository roleRepo) {

        logger.info("Executing validateCreate() " + " input : " + req);

        if (req == null) {
            throw new PersonNotValidException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());
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

    /**
     * Validates a PersonRequest for an update operation.
     *
     * @param id         the ID of the person to be updated
     * @param req        the PersonRequest to be validated
     * @param peopleRepo the PeopleRepository to be used for validation
     * @param roleRepo   the RoleRepository to be used for validation
     * @throws PersonNotValidException if the request is invalid
     */
    public static void validateUpdate(
            UUID id,
            PersonRequest req,
            PeopleRepository peopleRepo,
            RoleRepository roleRepo) {

        logger.info("Executing validateUpdate() " + " input : " + req);

        if (id == null)
            throw new PersonNotValidException(ErrorMessages.INVALID_ID.getMessage());
        if (req == null)
            throw new PersonNotValidException(ErrorMessages.REQUEST_BODY_REQUIRED.getMessage());

        // only validate fields that are provided (partial update)
        if (req.getFirstName() != null)
            requireNotBlank(req.getFirstName(), ErrorMessages.FIRST_NAME_REQUIRED.getMessage());
        if (req.getLastName() != null)
            requireNotBlank(req.getLastName(), ErrorMessages.LAST_NAME_REQUIRED.getMessage());

        if (req.getPrimaryEmail() != null) {
            requireNotBlank(req.getPrimaryEmail(), ErrorMessages.PRIMARY_EMAIL_REQUIRED.getMessage());
            String email = normalizeEmail(req.getPrimaryEmail());
            if (peopleRepo.existsByPrimaryEmailIgnoreCaseAndIdNot(email, id)) {
                throw new PersonNotValidException(ErrorMessages.PRIMARY_EMAIL_ALREADY_EXISTS.getMessage());
            }
        }

        if (req.getFirebaseUID() != null) {
            String uid = req.getFirebaseUID().trim();
            // allow clearing by ""
            if (!uid.isEmpty() && peopleRepo.existsByFirebaseUIDAndIdNot(uid, id)) {
                throw new PersonNotValidException(ErrorMessages.FIREBASE_UID_ALREADY_EXISTS.getMessage());
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
                throw new PersonNotValidException(ErrorMessages.FIREBASE_UID_ALREADY_EXISTS.getMessage());
            }
        }
    }

    private static void validateRolesExist(Set<String> roles, RoleRepository roleRepo) {

        if (roles == null)
            return;

        for (String role : roles) {

            if (isBlank(role))
                throw new PersonNotValidException(ErrorMessages.INVALID_ROLE.getMessage());

            String roleName = role.trim().toUpperCase();

            Optional<Role> roleOpt = roleRepo.findByName(roleName);

            if (!roleOpt.isPresent()) {
                throw new PersonNotValidException(ErrorMessages.INVALID_ROLE.getMessage());
            }
        }
    }

    private static void validateEmailUniquenessForCreate(PersonRequest req, PeopleRepository peopleRepo) {
        String email = normalizeEmail(req.getPrimaryEmail());
        if (peopleRepo.existsByPrimaryEmail(email)) {
            throw new PersonNotValidException(ErrorMessages.PRIMARY_EMAIL_ALREADY_EXISTS.getMessage());
        }
    }

    private static void requireNotBlank(String field, String message) {
        if (isBlank(field)) {
            throw new PersonNotValidException(message);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
