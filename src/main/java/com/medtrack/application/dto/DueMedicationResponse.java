package com.medtrack.application.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

// status is "PENDING", "TAKEN", or "SKIPPED" - PENDING is computed (no MedicationIntakeLog row
// exists yet for today), the other two mirror IntakeStatus once a row exists.
public record DueMedicationResponse(
        Long scheduleId,
        Long scheduleTimeId,
        String drugName,
        String doseText,
        LocalTime timeOfDay,
        LocalDate scheduledDate,
        String status,
        Instant confirmedAt) {
}
