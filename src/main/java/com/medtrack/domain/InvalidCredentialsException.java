package com.medtrack.domain;

// Thrown by standard (name/surname/email/password) login when the email is known but the name or
// password don't match. Deliberately generic (no field-level detail) - unlike ID-Austria
// verification, login shouldn't reveal which part of the submitted credentials was wrong.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Die eingegebenen Daten stimmen nicht mit dem gespeicherten Konto überein.");
    }
}
