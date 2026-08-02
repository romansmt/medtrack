package com.medtrack.application.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record CreateScheduleRequest(
        Long drugId,
        String doseText,
        LocalDate startDate,
        LocalDate endDate,
        List<LocalTime> times) {
}
