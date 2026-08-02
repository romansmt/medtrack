package com.medtrack.application.dto;

import java.time.LocalDate;
import java.util.List;

public record MedicationScheduleResponse(
        Long id,
        Long drugId,
        String drugName,
        String doseText,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        List<ScheduleTimeResponse> times) {
}
