package com.medtrack.application.dto;

// Compared against a Patient's own stored name/email/password - a check that's entirely separate
// from ID-Austria verification. See RegistrationService.loginStandard.
public record StandardLoginRequest(String firstName, String lastName, String email, String password) {
}
