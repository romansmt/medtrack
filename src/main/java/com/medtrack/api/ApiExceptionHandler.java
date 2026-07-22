package com.medtrack.api;

import com.medtrack.domain.PatientNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Void> handlePatientNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
