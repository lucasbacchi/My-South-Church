package com.southchurch.my.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.dto.UpdatePersonCommand;
import com.southchurch.my.services.person.CreatePersonService;
import com.southchurch.my.services.person.GetCurrentPersonService;
import com.southchurch.my.services.person.GetPeopleService;
import com.southchurch.my.services.person.GetPersonByEmailService;
import com.southchurch.my.services.person.GetPersonByFirebaseUIDService;
import com.southchurch.my.services.person.GetPersonService;
import com.southchurch.my.services.person.UpdatePersonService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final CreatePersonService createPersonService;
    private final GetPeopleService getPeopleService;
    private final GetPersonByFirebaseUIDService getPersonByFirebaseUID;
    private final GetPersonByEmailService getPersonByEmailService;
    private final GetPersonService getPersonService;
    private final UpdatePersonService updatePersonService;
    private final GetCurrentPersonService getCurrentPersonService;

    public PeopleController(
            CreatePersonService createPersonService,
            GetPeopleService getPeopleService,
            GetPersonByFirebaseUIDService getPersonByFirebaseUID,
            GetPersonByEmailService getPersonByEmailService,
            GetPersonService getPersonService,
            UpdatePersonService updatePersonService,
            GetCurrentPersonService getCurrentPersonService) {
        this.createPersonService = createPersonService;
        this.getPeopleService = getPeopleService;
        this.getPersonByFirebaseUID = getPersonByFirebaseUID;
        this.getPersonByEmailService = getPersonByEmailService;
        this.getPersonService = getPersonService;
        this.updatePersonService = updatePersonService;
        this.getCurrentPersonService = getCurrentPersonService;
    }

    @PostMapping("/create")
    public ResponseEntity<PersonResponse> createPerson(@RequestBody PersonRequest input) {
        return createPersonService.execute(input);
    }

    @GetMapping("")
    public ResponseEntity<List<PersonResponse>> getPeople() {
        return getPeopleService.execute(null);
    }

    @GetMapping("/firebase/{uid}")
    public ResponseEntity<PersonResponse> getPersonByFirebaseUID(@PathVariable String uid) {
        return getPersonByFirebaseUID.execute(uid);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<PersonResponse> getPersonByPrimaryEmail(@PathVariable("email") String primaryEmail) {
        return getPersonByEmailService.execute(primaryEmail);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<PersonResponse> getPerson(@PathVariable UUID id) {
        return getPersonService.execute(id);
    }

    @GetMapping("/me")
    public ResponseEntity<PersonResponse> getCurrentPerson(JwtAuthenticationToken authentication) {
        return getCurrentPersonService.execute(authentication);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<PersonResponse> updatePerson(@PathVariable UUID id, @RequestBody PersonRequest request) {
        return updatePersonService.execute(new UpdatePersonCommand(id, request));
    }
}
