package com.medtrack.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AvailabilityItemResult(
        Long drugId,
        String drugName,
        String form,
        String packSize,
        int requestedQuantity,
        boolean inStock,
        BigDecimal price,
        Instant lastUpdated) {
}
