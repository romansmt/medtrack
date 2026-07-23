package com.medtrack.application.dto;

import java.time.LocalTime;

public record OpeningHoursResponse(
        String dayOfWeek,
        LocalTime opensAt,
        LocalTime closesAt,
        String kind) {
}
