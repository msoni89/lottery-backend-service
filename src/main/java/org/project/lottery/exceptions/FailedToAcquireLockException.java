package org.project.lottery.exceptions;

public class FailedToAcquireLockException extends RuntimeException {
    public FailedToAcquireLockException(String message) {
        super(message);
    }
}
