package com.medtrack.application.dto;

import java.util.List;

public record PharmacyAvailabilityResponse(
        PharmacyResponse pharmacy,
        boolean allAvailable,
        List<AvailabilityItemResult> items) {
}
