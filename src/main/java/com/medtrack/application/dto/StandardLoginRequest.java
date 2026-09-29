package com.medtrack.application.dto;

// Password is intentionally not part of this DTO - see RegistrationController for why.
public record StandardLoginRequest(String email) {
}
