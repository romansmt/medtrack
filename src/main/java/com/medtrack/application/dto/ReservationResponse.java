package com.medtrack.application.dto;

import java.time.Instant;

public record ReservationResponse(
        Long id,
        Long pharmacyId,
        String pharmacyName,
        Long drugId,
        String drugName,
        int quantity,
        String status,
        Instant requestedAt) {
}
