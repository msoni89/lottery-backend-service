package org.project.lottery.exceptions;

public class UserAlreadyIssuedTicketException extends RuntimeException {
    public UserAlreadyIssuedTicketException(String message) {
        super(message);
    }
}
