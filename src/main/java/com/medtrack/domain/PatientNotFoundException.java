package com.medtrack.domain;

public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String svnr) {
        super("No patient found for SVNR " + svnr);
    }
}
