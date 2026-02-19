package com.southchurch.my.services.person;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.Command;
import com.southchurch.my.dto.UpdatePersonCommand;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.exceptions.people.PersonNotFoundException;
import com.southchurch.my.exceptions.people.PersonNotValidException;
import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.models.Person;
import com.southchurch.my.models.Role;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.repositories.RoleRepository;
import com.southchurch.my.security.FirebaseCustomClaimsService;
import com.southchurch.my.validators.PersonValidator;

@Service
public class UpdatePersonService implements Command<UpdatePersonCommand, PersonResponse> {

    private final PeopleRepository peopleRepo;
    private final RoleRepository roleRepo;
    private final FirebaseCustomClaimsService firebaseClaimsService;
    private final VerifyGoogleAccountService verifyGoogleAccountService;

    public UpdatePersonService(PeopleRepository peopleRepo, RoleRepository roleRepo,
            FirebaseCustomClaimsService firebaseClaimsService,
            VerifyGoogleAccountService verifyGoogleAccountService) {
        this.peopleRepo = peopleRepo;
        this.roleRepo = roleRepo;
        this.firebaseClaimsService = firebaseClaimsService;
        this.verifyGoogleAccountService = verifyGoogleAccountService;
    }

    @Override
    @Transactional
    @Caching(put = @CachePut(value = "personByIdCache", key = "#input.id"), evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<PersonResponse> execute(UpdatePersonCommand input) {

        PersonRequest req = input.getRequest();
        UUID id = input.getId();

        PersonValidator.validateUpdate(id, req, peopleRepo, roleRepo);

        Person person = peopleRepo.findById(id)
                .orElseThrow(() -> new PersonNotFoundException());

        boolean emailChanged = false;

        if (req.getFirstName() != null)
            person.setFirstName(req.getFirstName().trim());

        if (req.getLastName() != null)
            person.setLastName(req.getLastName().trim());

        if (req.getPrimaryEmail() != null) {
            String oldEmail = person.getPrimaryEmail();
            String newEmail = req.getPrimaryEmail().trim().toLowerCase();
            if (!oldEmail.equalsIgnoreCase(newEmail)) {
                emailChanged = true;
            }
            person.setPrimaryEmail(newEmail);
        }

        if (req.getSecondaryEmail() != null) {
            String se = req.getSecondaryEmail().trim();
            person.setSecondaryEmail(se.isEmpty() ? null : se);
        }

        if (req.getPhoneNumber() != null) {
            String pn = req.getPhoneNumber().trim();
            person.setPhoneNumber(pn.isEmpty() ? null : pn);
        }

        if (req.getDateOfBirth() != null)
            person.setDateOfBirth(req.getDateOfBirth());

        if (req.getFirebaseUID() != null) {
            String uid = req.getFirebaseUID().trim();
            person.setFirebaseUID(uid.isEmpty() ? null : uid);
        }

        if (req.getRoles() != null) {
            person.getRoles().clear();

            req.getRoles().forEach(roleName -> {
                Role role = roleRepo.findByName(roleName.trim().toUpperCase())
                        .orElseThrow(() -> new PersonNotValidException(ErrorMessages.INVALID_ROLE.getMessage()));
                person.addRole(role);
            });
        }

        Person updated = peopleRepo.save(person);

        // Sync roles to Firebase custom claims if person has Firebase UID
        if (updated.getFirebaseUID() != null && !updated.getFirebaseUID().isEmpty()) {
            List<String> roles = updated.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            firebaseClaimsService.syncRolesToFirebase(updated.getFirebaseUID(), roles);
        }

        // Re-verify Google Account if email changed
        if (emailChanged) {
            Boolean verified = verifyGoogleAccountService.verify(updated.getPrimaryEmail());
            updated.setGoogleAccountVerified(verified);
            peopleRepo.save(updated);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new PersonResponse(updated));
    }
}
