package com.medtrack.application.dto;

import java.time.LocalDate;

// Shared by both registration paths: the ID-Austria "Registrieren" tab sends email as null, the
// standard (email/password) registration form populates it - see RegistrationController.
public record CreateAccountRequest(String fullName, LocalDate dateOfBirth, String email) {
}
