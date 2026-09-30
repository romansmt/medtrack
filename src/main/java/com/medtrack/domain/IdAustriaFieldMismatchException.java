package com.medtrack.domain;

import java.util.List;

// Thrown when the submitted SVNR is found in the ID-Austria registry but one or more of the other
// submitted fields (name, birthdate, card serial, carrier number, insurer, expiry) don't match the
// registry record. Carries every mismatched field, not just the first one, so the user is told
// everything that's wrong in a single attempt.
public class IdAustriaFieldMismatchException extends RuntimeException {

    public IdAustriaFieldMismatchException(List<String> mismatchedFields) {
        super("Folgende Angaben stimmen nicht mit dem ID-Austria-Register überein: " + String.join(", ", mismatchedFields));
    }
}
