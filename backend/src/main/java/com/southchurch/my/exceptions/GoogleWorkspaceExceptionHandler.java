package com.southchurch.my.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GoogleWorkspaceExceptionHandler {

    @ExceptionHandler(GoogleWorkspaceException.class)
    public void handleGoogleWorkspaceException(GoogleWorkspaceException ex) {

        HttpStatus status = switch (ex.getGoogleStatus()){
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_GATEWAY; // Google failure
        };

        throw new ResponseStatusException(status, ex.getMessage(), ex); 
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleIllegalArg(IllegalArgumentException ex) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
    }
}
