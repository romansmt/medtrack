package com.medtrack.domain;

public class PatientEmailAlreadyExistsException extends RuntimeException {

    public PatientEmailAlreadyExistsException(String email) {
        super("A patient with email " + email + " is already registered");
    }
}
