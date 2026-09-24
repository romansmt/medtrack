package com.medtrack.application.dto;

import java.time.Instant;
import java.time.LocalDate;

public record IdAustriaLoginResponse(String fullName, LocalDate dateOfBirth, Instant authenticatedAt) {
}
