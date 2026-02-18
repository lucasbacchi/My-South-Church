package com.southchurch.my.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.dto.PersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.dto.UpdatePersonCommand;
import com.southchurch.my.dto.imports.ImportPeopleResult;
import com.southchurch.my.dto.imports.ImportProfileRecord;
import com.southchurch.my.services.person.CreatePersonService;
import com.southchurch.my.services.person.DeletePersonService;
import com.southchurch.my.services.person.GetCurrentPersonService;
import com.southchurch.my.services.person.GetPeopleService;
import com.southchurch.my.services.person.GetPersonByEmailService;
import com.southchurch.my.services.person.GetPersonByFirebaseUIDService;
import com.southchurch.my.services.person.GetPersonService;
import com.southchurch.my.services.person.ImportPeopleService;
import com.southchurch.my.services.person.UpdatePersonService;

import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final DeletePersonService deletePersonService;
    private final ImportPeopleService importPeopleService;

    public PeopleController(
            CreatePersonService createPersonService,
            GetPeopleService getPeopleService,
            GetPersonByFirebaseUIDService getPersonByFirebaseUID,
            GetPersonByEmailService getPersonByEmailService,
            GetPersonService getPersonService,
            UpdatePersonService updatePersonService,
            GetCurrentPersonService getCurrentPersonService,
            DeletePersonService deletePersonService,
            ImportPeopleService importPeopleService) {
        this.createPersonService = createPersonService;
        this.getPeopleService = getPeopleService;
        this.getPersonByFirebaseUID = getPersonByFirebaseUID;
        this.getPersonByEmailService = getPersonByEmailService;
        this.getPersonService = getPersonService;
        this.updatePersonService = updatePersonService;
        this.getCurrentPersonService = getCurrentPersonService;
        this.deletePersonService = deletePersonService;
        this.importPeopleService = importPeopleService;
    }

    @PostMapping("")
    @PreAuthorize("@authorizationService.canCreatePerson(authentication)")
    public ResponseEntity<PersonResponse> createPerson(@RequestBody PersonRequest input) {
        return createPersonService.execute(input);
    }

    @PostMapping("/import")
    @PreAuthorize("@authorizationService.canCreatePerson(authentication)")
    public ResponseEntity<ImportPeopleResult> importPeople(@RequestBody List<ImportProfileRecord> records) {
        return importPeopleService.execute(records);
    }

    @GetMapping("")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<List<PersonResponse>> getPeople() {
        return getPeopleService.execute(null);
    }

    @GetMapping("/firebase/{uid}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPersonByFirebaseUID(@PathVariable String uid) {
        return getPersonByFirebaseUID.execute(uid);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPersonByPrimaryEmail(@PathVariable("email") String primaryEmail) {
        return getPersonByEmailService.execute(primaryEmail);
    }

    @GetMapping("/id/{id}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPerson(@PathVariable UUID id) {
        return getPersonService.execute(id);
    }

    @GetMapping("/me")
    public ResponseEntity<PersonResponse> getCurrentPerson(JwtAuthenticationToken authentication) {
        return getCurrentPersonService.execute(authentication);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.canUpdatePerson(authentication)")
    public ResponseEntity<PersonResponse> updatePerson(@PathVariable UUID id, @RequestBody PersonRequest request) {
        return updatePersonService.execute(new UpdatePersonCommand(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.canDeletePerson(authentication)")
    public ResponseEntity<Void> deletePerson(@PathVariable UUID id) {
        return deletePersonService.execute(id);
    }
}
