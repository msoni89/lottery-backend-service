package org.project.lottery.exceptions;

public class OutOfTicketsException extends RuntimeException {
    public OutOfTicketsException(String message) {
        super(message);
    }
}
