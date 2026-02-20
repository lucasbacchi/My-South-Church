package com.southchurch.my.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleWorkspaceException extends RuntimeException {

    private final int googleStatus;
    private static final Logger logger = LoggerFactory.getLogger(GoogleWorkspaceException.class);

    public GoogleWorkspaceException(String message, int googleStatus, Throwable cause) {
        super(message, cause);
        this.googleStatus = googleStatus;
        logger.error("Exception " + getClass() + " thrown: " + message);
    }

    public int getGoogleStatus() {
        return googleStatus;
    }
}
