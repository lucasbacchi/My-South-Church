package com.southchurch.my.services.person;

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
import com.southchurch.my.validators.PersonValidator;

@Service
public class CreatePersonService implements Command<PersonRequest, PersonResponse> {

    private final PeopleRepository repository;
    private final RoleRepository roleRepository;

    public CreatePersonService(PeopleRepository repository, RoleRepository roleRepository) {
        this.repository = repository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
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

        return ResponseEntity.status(HttpStatus.CREATED).body(new PersonResponse(savedPerson));
    }
}
