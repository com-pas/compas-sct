package org.lfenergy.compas.sct.exception;


public class CompasDataAccessException extends Exception {

    public CompasDataAccessException(String message) {
        super(message);
    }

    public CompasDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
