package com.southchurch.my.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GoogleWorkspaceExceptionHandler {

    /**
     * Handle Google Workspace exception and return appropriate HTTP response.
     * 
     * @param ex The Google Workspace exception to handle.
     * @return A ResponseEntity containing a ProblemDetail with the appropriate HTTP status and detail.
     */
    @ExceptionHandler(GoogleWorkspaceException.class)
    public ResponseEntity<ProblemDetail> handleGoogleWorkspaceException(GoogleWorkspaceException ex) {
        HttpStatus status = switch (ex.getGoogleStatus()) {
            case 400 -> HttpStatus.BAD_REQUEST;
            case 401 -> HttpStatus.UNAUTHORIZED;
            case 403 -> HttpStatus.FORBIDDEN;
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_GATEWAY; // Google failure
        };
        ProblemDetail body = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Handle Illegal Argument Exception and return appropriate HTTP response.
     * 
     * @return A ResponseEntity containing a ProblemDetail with the appropriate HTTP status and detail.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArg(IllegalArgumentException ex) {
        ProblemDetail body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
