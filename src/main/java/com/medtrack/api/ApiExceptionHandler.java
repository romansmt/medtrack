package com.medtrack.api;

import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.domain.ReservationNotSupportedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Void> handlePatientNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Void> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(ReservationNotSupportedException.class)
    public ResponseEntity<String> handleReservationNotSupported(ReservationNotSupportedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }
}
