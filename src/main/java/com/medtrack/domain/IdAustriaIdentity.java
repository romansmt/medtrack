package com.medtrack.domain;

import java.time.Instant;
import java.time.LocalDate;

public record IdAustriaIdentity(String fullName, LocalDate dateOfBirth, Instant authenticatedAt) {
}
