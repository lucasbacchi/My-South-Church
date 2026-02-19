package com.southchurch.my.exceptions.people;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.southchurch.my.exceptions.ErrorMessages;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PersonNotFoundException extends RuntimeException {

    private static final  Logger logger = LoggerFactory.getLogger(PersonNotFoundException.class);

    public PersonNotFoundException() {
        super(ErrorMessages.PERSON_NOT_FOUND.getMessage());
        logger.error("Exception " + getClass() + " thrown: " + ErrorMessages.PERSON_NOT_FOUND.getMessage());
    }

}
