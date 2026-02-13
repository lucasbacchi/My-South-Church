package com.southchurch.my.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PersonNotValidException extends RuntimeException {

    public PersonNotValidException(String message) {
        super(message);
    }

}
