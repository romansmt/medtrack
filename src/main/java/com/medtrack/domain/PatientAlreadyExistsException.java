package com.medtrack.domain;

public class PatientAlreadyExistsException extends RuntimeException {

    public PatientAlreadyExistsException(String fullName) {
        super("A patient named " + fullName + " is already registered");
    }
}
