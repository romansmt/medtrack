package com.medtrack.application.dto;

// status must be "TAKEN" or "SKIPPED" (matches domain.IntakeStatus).
public record RecordIntakeRequest(String status) {
}
