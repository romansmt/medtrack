package com.medtrack.application.dto;

public record DrugResponse(
        Long id,
        String name,
        String activeSubstance,
        String form,
        String packSize,
        String manufacturer,
        boolean prescriptionRequired,
        String pzn) {
}
