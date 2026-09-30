package com.medtrack.domain;

// Thrown when a submitted SVNR doesn't match any row in the (mock) ID-Austria registry - the
// "wrong SVNR" alert during registration/login verification.
public class IdAustriaRecordNotFoundException extends RuntimeException {

    public IdAustriaRecordNotFoundException(String svnr) {
        super("Es wurde keine ID-Austria-Identität mit der SVNR " + svnr + " gefunden. Bitte überprüfen Sie die SVNR.");
    }
}
