package com.southchurch.my.exceptions.people;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PersonNotValidException extends RuntimeException {

    private static final  Logger logger = LoggerFactory.getLogger(PersonNotValidException.class);

    public PersonNotValidException(String message) {
        super(message);
        logger.error("Exception " + getClass() + " thrown");

    }

}
