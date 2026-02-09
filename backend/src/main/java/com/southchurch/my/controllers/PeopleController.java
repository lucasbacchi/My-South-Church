package com.southchurch.my.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.southchurch.my.dto.CreatePersonRequest;
import com.southchurch.my.dto.PersonResponse;
import com.southchurch.my.services.person.CreatePersonService;

@RestController
@RequestMapping("/people")
public class PeopleController {

    private final CreatePersonService createPersonService;

    public PeopleController(CreatePersonService createPersonService) {
        this.createPersonService = createPersonService;
    }

    @PostMapping("/create")
    public ResponseEntity<PersonResponse> createPerson(@RequestBody CreatePersonRequest input) {
        return createPersonService.execute(input);
    }

}
