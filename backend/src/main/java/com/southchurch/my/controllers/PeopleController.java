package com.southchurch.my.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import com.southchurch.my.services.person.CreatePersonService;
import com.southchurch.my.services.person.DeletePersonService;
import com.southchurch.my.services.person.GetCurrentPersonService;
import com.southchurch.my.services.person.GetPeopleService;
import com.southchurch.my.services.person.GetPersonByEmailService;
import com.southchurch.my.services.person.GetPersonByFirebaseUIDService;
import com.southchurch.my.services.person.GetPersonService;
import com.southchurch.my.services.person.ImportPeopleService;
import com.southchurch.my.services.person.UpdatePersonService;
import com.southchurch.my.services.person.VerifyGoogleAccountService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.southchurch.my.models.Person;
import com.southchurch.my.repositories.PeopleRepository;

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
    private final VerifyGoogleAccountService verifyGoogleAccountService;
    private final PeopleRepository peopleRepository;

    public PeopleController(
            CreatePersonService createPersonService,
            GetPeopleService getPeopleService,
            GetPersonByFirebaseUIDService getPersonByFirebaseUID,
            GetPersonByEmailService getPersonByEmailService,
            GetPersonService getPersonService,
            UpdatePersonService updatePersonService,
            GetCurrentPersonService getCurrentPersonService,
            DeletePersonService deletePersonService,
            ImportPeopleService importPeopleService,
            VerifyGoogleAccountService verifyGoogleAccountService,
            PeopleRepository peopleRepository) {
        this.createPersonService = createPersonService;
        this.getPeopleService = getPeopleService;
        this.getPersonByFirebaseUID = getPersonByFirebaseUID;
        this.getPersonByEmailService = getPersonByEmailService;
        this.getPersonService = getPersonService;
        this.updatePersonService = updatePersonService;
        this.getCurrentPersonService = getCurrentPersonService;
        this.deletePersonService = deletePersonService;
        this.importPeopleService = importPeopleService;
        this.verifyGoogleAccountService = verifyGoogleAccountService;
        this.peopleRepository = peopleRepository;
    }

    /**
     * Creates a Person in the database from a given PersonRequest.
     * 
     * @param input the PersonRequest to be created into a Person
     * @return a ResponseEntity containing a PersonResponse or an error if the call
     *         fails
     * @throws PersonNotFoundException if the person does not exist in the database
     * @throws PersonNotValidException if the person is not valid
     */
    @PostMapping("")
    @PreAuthorize("@authorizationService.canCreatePerson(authentication)")
    public ResponseEntity<PersonResponse> createPerson(@RequestBody PersonRequest input) {
        return createPersonService.execute(input);
    }

    /**
     * Imports a list of people records into the database from a given list of
     * JsonNode records.
     * 
     * @param records the list of JsonNode records to be imported into the database
     * @return a ResponseEntity containing an ImportPeopleResult object, or an error
     *         if the call fails
     */
    @PostMapping("/import")
    @PreAuthorize("@authorizationService.canCreatePerson(authentication)")
    public ResponseEntity<ImportPeopleResult> importPeople(@RequestBody List<JsonNode> records) {
        return importPeopleService.execute(records);
    }

    /**
     * Fetches a list of all Person records in the database.
     * 
     * This method is accessible by admins only.
     * 
     * @return a ResponseEntity containing a list of PersonResponse objects, or an
     *         error if the call fails
     */
    @GetMapping("")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<List<PersonResponse>> getPeople() {
        return getPeopleService.execute(null);
    }

    /**
     * Exports a list of all Person records in the database as a JSON file.
     * 
     * This method is accessible by admins only.
     * 
     * @return a ResponseEntity containing a list of PersonResponse objects, or an
     *         error if the call fails
     */
    @GetMapping("/export")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<List<PersonResponse>> exportPeople() {
        List<PersonResponse> people = getPeopleService.execute(null).getBody();
        if (people == null) {
            people = List.of();
        }

        String date = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        String filename = "people-export-" + date + ".json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(people);
    }

    /**
     * Fetches a person record in the database by their Firebase UID.
     * 
     * This method is accessible by admins only.
     * 
     * @param uid the Firebase UID of the person to fetch
     * @return a ResponseEntity containing a PersonResponse or null if the user was
     *         not found
     * @throws PersonNotFoundException if the user was not found
     */
    @GetMapping("/firebase/{uid}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPersonByFirebaseUID(@PathVariable String uid) {
        return getPersonByFirebaseUID.execute(uid);
    }

    /**
     * Fetches a person record in the database by their primary email address.
     * 
     * This method is accessible by admins only.
     * 
     * @param primaryEmail the primary email address of the person to fetch
     * @return a ResponseEntity containing a PersonResponse or null if the user was
     *         not found
     * @throws PersonNotFoundException if the user was not found
     */
    @GetMapping("/email/{email}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPersonByPrimaryEmail(@PathVariable("email") String primaryEmail) {
        return getPersonByEmailService.execute(primaryEmail);
    }

    /**
     * Fetches a person record in the database by their UUID.
     * 
     * This method is accessible by admins only.
     * 
     * @param id the UUID of the person to fetch
     * @return a ResponseEntity containing a PersonResponse or null if the user was
     *         not found
     * @throws PersonNotFoundException if the user was not found
     */
    @GetMapping("/id/{id}")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    public ResponseEntity<PersonResponse> getPerson(@PathVariable UUID id) {
        return getPersonService.execute(id);
    }

    /**
     * Fetches a person record in the database by their authentication token.
     * 
     * This method is accessible by authenticated users only.
     * 
     * @param authentication the authentication token to fetch the person
     * @return a ResponseEntity containing a PersonResponse or null if the user was
     *         not found
     * @throws PersonNotFoundException if the user was not found
     */
    @GetMapping("/me")
    public ResponseEntity<PersonResponse> getCurrentPerson(JwtAuthenticationToken authentication) {
        return getCurrentPersonService.execute(authentication);
    }

    /**
     * Updates a person record in the database by their UUID.
     * 
     * This method is accessible by authenticated users only if they have the
     * necessary permissions.
     * 
     * @param id      the UUID of the person to update
     * @param request the PersonRequest containing the updated information
     * @return a ResponseEntity containing a PersonResponse or an error if the call
     *         fails
     * @throws PersonNotFoundException if the person does not exist in the database
     * @throws PersonNotValidException if the person is not valid
     */
    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.canUpdatePerson(authentication)")
    public ResponseEntity<PersonResponse> updatePerson(@PathVariable UUID id, @RequestBody PersonRequest request) {
        return updatePersonService.execute(new UpdatePersonCommand(id, request));
    }

    /**
     * Deletes a person record in the database by their UUID.
     * 
     * This method is accessible by authenticated users only if they have the
     * necessary permissions.
     * 
     * @param id the UUID of the person to delete
     * @return a ResponseEntity containing the status of the operation
     * @throws PersonNotFoundException if the person does not exist in the database
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.canDeletePerson(authentication)")
    public ResponseEntity<Void> deletePerson(@PathVariable UUID id) {
        return deletePersonService.execute(id);
    }

    /**
     * Verifies a person's Google account by their primary email address.
     * 
     * This method is accessible by admins only.
     * 
     * @param id the UUID of the person to verify
     * @return a ResponseEntity containing a PersonResponse or an error if the call
     *         fails
     * @throws PersonNotFoundException if the person does not exist in the database
     */
    @PostMapping("/{id}/verify-google-account")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    @Caching(put = @CachePut(value = "personByIdCache", key = "#id"), evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<PersonResponse> verifyGoogleAccount(@PathVariable UUID id) {
        Person person = peopleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found"));

        Boolean verified = verifyGoogleAccountService.verify(person.getPrimaryEmail());
        person.setGoogleAccountVerified(verified);
        peopleRepository.save(person);

        return ResponseEntity.ok(new PersonResponse(person));
    }

    /**
     * Verifies all people in the database by their primary email address.
     * 
     * This method is accessible by admins only.
     * 
     * @return a ResponseEntity containing a string message indicating the result of
     *         the verification process
     */
    @PostMapping("/verify-all-google-accounts")
    @PreAuthorize("@authorizationService.isAdmin(authentication)")
    @Caching(evict = {
            @CacheEvict(value = "peopleAllCache", allEntries = true),
            @CacheEvict(value = "personByIdCache", allEntries = true),
            @CacheEvict(value = "personByEmailCache", allEntries = true),
            @CacheEvict(value = "personByFirebaseCache", allEntries = true)
    })
    public ResponseEntity<String> verifyAllGoogleAccounts() {
        List<Person> people = peopleRepository.findAll();
        int verified = 0;
        int notVerified = 0;
        int indeterminate = 0;

        for (Person person : people) {
            // Skip if already verified (to save API calls)
            if (Boolean.TRUE.equals(person.getGoogleAccountVerified())) {
                continue;
            }

            Boolean result = verifyGoogleAccountService.verify(person.getPrimaryEmail());
            person.setGoogleAccountVerified(result);
            peopleRepository.save(person);

            if (Boolean.TRUE.equals(result)) {
                verified++;
            } else if (Boolean.FALSE.equals(result)) {
                notVerified++;
            } else {
                indeterminate++;
            }
        }

        String message = String.format("Verification complete: %d verified, %d not verified, %d indeterminate",
                verified, notVerified, indeterminate);
        return ResponseEntity.ok(message);
    }
}
