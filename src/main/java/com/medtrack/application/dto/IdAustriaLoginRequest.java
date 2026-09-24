package com.medtrack.application.dto;

import java.time.LocalDate;

public record IdAustriaLoginRequest(String fullName, LocalDate dateOfBirth) {
}
