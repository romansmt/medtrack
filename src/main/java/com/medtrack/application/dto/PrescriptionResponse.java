package com.medtrack.application.dto;

import com.medtrack.domain.PrescriptionStatus;

import java.time.LocalDate;

public record PrescriptionResponse(
        String drug,
        String dosage,
        String doctorName,
        LocalDate issuedDate,
        PrescriptionStatus status) {
}
