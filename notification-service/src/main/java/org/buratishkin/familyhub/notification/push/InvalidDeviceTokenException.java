package org.buratishkin.familyhub.notification.push;

public class InvalidDeviceTokenException extends RuntimeException {
    public InvalidDeviceTokenException(String message) {
        super(message);
    }
}
