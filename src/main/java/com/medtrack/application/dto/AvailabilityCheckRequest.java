package com.medtrack.application.dto;

import java.util.List;

public record AvailabilityCheckRequest(
        List<AvailabilityRequestItem> items,
        double latitude,
        double longitude,
        Double radiusKm) {
}
