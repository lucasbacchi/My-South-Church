package com.southchurch.my.services.person;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.southchurch.my.Command;
import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.exceptions.ErrorMessages;
import com.southchurch.my.models.Person;
import com.southchurch.my.models.Role;
import com.southchurch.my.repositories.PeopleRepository;
import com.southchurch.my.repositories.RoleRepository;
import com.southchurch.my.security.FirebaseCustomClaimsService;
import com.southchurch.my.validators.PersonValidator;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CreatePersonService implements Command<PersonRequest, PersonResponse> {

    private final PeopleRepository repository;
    private final RoleRepository roleRepository;
    private final FirebaseCustomClaimsService firebaseClaimsService;
    private final VerifyGoogleAccountService verifyGoogleAccountService;

    public CreatePersonService(PeopleRepository repository, RoleRepository roleRepository,
            FirebaseCustomClaimsService firebaseClaimsService,
            VerifyGoogleAccountService verifyGoogleAccountService) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.firebaseClaimsService = firebaseClaimsService;
        this.verifyGoogleAccountService = verifyGoogleAccountService;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<PersonResponse> execute(PersonRequest input) {

        PersonValidator.validateCreate(input, repository, roleRepository);

        Person person = new Person(input);

        if (input.getRoles() != null && !input.getRoles().isEmpty()) {
            input.getRoles().forEach(roleRaw -> {
                if (roleRaw == null)
                    return;

                String roleName = roleRaw.trim().toUpperCase();

                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException(ErrorMessages.INVALID_ROLE.getMessage()));

                person.addRole(role);
            });
        }

        Person savedPerson = repository.save(person);

        // Sync roles to Firebase custom claims if user has a Firebase UID
        if (savedPerson.getFirebaseUID() != null && !savedPerson.getFirebaseUID().isBlank()) {
            List<String> roleNames = savedPerson.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());
            firebaseClaimsService.syncRolesToFirebase(savedPerson.getFirebaseUID(), roleNames);
        }

        // Auto-verify Google Account on creation
        Boolean verified = verifyGoogleAccountService.verify(savedPerson.getPrimaryEmail());
        savedPerson.setGoogleAccountVerified(verified);
        repository.save(savedPerson);

        return ResponseEntity.status(HttpStatus.CREATED).body(new PersonResponse(savedPerson));
    }
}
