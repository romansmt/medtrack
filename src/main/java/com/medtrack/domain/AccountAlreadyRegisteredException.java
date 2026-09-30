package com.medtrack.domain;

// Thrown when registration is attempted for an identity that already has a MedTrack account
// (a Patient row with a password hash already set) - the "user already exists" alert.
public class AccountAlreadyRegisteredException extends RuntimeException {

    public AccountAlreadyRegisteredException(String svnr) {
        super("Für diese Identität existiert bereits ein MedTrack-Konto. Bitte melden Sie sich stattdessen an.");
    }
}
