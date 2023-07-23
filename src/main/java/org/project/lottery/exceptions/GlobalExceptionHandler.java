package org.project.lottery.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Handles {@link NotFoundException} exceptions.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with the error message and status code
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(NotFoundException ex) {
        // Create a map to store the error message and status code
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.NOT_FOUND);

        // Return a {@link ResponseEntity} with the error message and status code
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles {@link FailedToAcquireLockException} exceptions.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with the error message and status code
     */
    @ExceptionHandler(FailedToAcquireLockException.class)
    public ResponseEntity<Object> handleResourceFailedToAcquireLockException(FailedToAcquireLockException ex) {
        // Create a map to store the error message and status code
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.LOCKED);

        // Return a {@link ResponseEntity} with the error message and status code
        return new ResponseEntity<>(body, HttpStatus.LOCKED);
    }

    /**
     * Handles {@link OutOfTicketsException} exceptions.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with the error message and status code
     */
    @ExceptionHandler(OutOfTicketsException.class)
    public ResponseEntity<Object> handleResourceOutOfTicketsException(OutOfTicketsException ex) {
        // Create a map to store the error message and status code
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.GONE);

        // Return a {@link ResponseEntity} with the error message and status code
        return new ResponseEntity<>(body, HttpStatus.GONE);
    }

    /**
     * Handles {@link UserAlreadyIssuedTicketException} exceptions.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with the error message and status code
     */
    @ExceptionHandler(UserAlreadyIssuedTicketException.class)
    public ResponseEntity<Object> handleResourceUserAlreadyIssuedTicketException(UserAlreadyIssuedTicketException ex) {
        // Create a map to store the error message and status code
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("status", HttpStatus.FORBIDDEN);

        // Return a {@link ResponseEntity} with the error message and status code
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles method arg exceptions.
     *
     * @param ex the exception
     * @return a {@link ResponseEntity} with the error message and status code
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // Create a map to store the error messages
        Map<String, String> errors = new HashMap<>(ex.getBindingResult().getAllErrors().size());

        // Iterate over the errors and add them to the map
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String key = ((FieldError) error).getField();
            String val = error.getDefaultMessage();
            errors.put(key, val);
        });

        // Add the status code to the map
        errors.put("status", HttpStatus.BAD_REQUEST.toString());

        // Return a {@link ResponseEntity} with the error messages and status code
        return ResponseEntity.badRequest().body(errors);
    }
}
