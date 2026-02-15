package com.southchurch.my.exceptions.people;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.southchurch.my.exceptions.ErrorMessages;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends RuntimeException {

    public PersonNotFoundException() {
        super(ErrorMessages.PERSON_NOT_FOUND.getMessage());
    }

}
