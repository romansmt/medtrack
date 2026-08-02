package com.medtrack.application.dto;

import java.math.BigDecimal;

public record PriceComparisonEntry(
        PharmacyResponse pharmacy,
        BigDecimal price,
        boolean inStock) {
}
