package com.medtrack.application.dto;

import java.util.List;

public record PharmacyDetailResponse(
        PharmacyResponse pharmacy,
        List<OpeningHoursResponse> openingHours) {
}
