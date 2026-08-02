package com.medtrack.application.dto;

import java.time.LocalTime;

public record ScheduleTimeResponse(Long id, LocalTime timeOfDay) {
}
