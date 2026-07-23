package com.medtrack.application.dto;

import java.math.BigDecimal;

public record AvailabilityItemResult(
        Long drugId,
        String drugName,
        String form,
        String packSize,
        int requestedQuantity,
        boolean inStock,
        BigDecimal price) {
}
