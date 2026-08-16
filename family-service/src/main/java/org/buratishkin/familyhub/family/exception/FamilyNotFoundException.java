package org.buratishkin.familyhub.family.exception;

public class FamilyNotFoundException extends RuntimeException {
    public FamilyNotFoundException(String message) {
        super(message);
    }
}
