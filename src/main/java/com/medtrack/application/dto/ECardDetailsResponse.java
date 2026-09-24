package com.medtrack.application.dto;

import java.time.LocalDate;

public record ECardDetailsResponse(
        String svnr,
        String medtrackId,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String cardSerialNumber,
        String carrierNumber,
        String carrierName,
        LocalDate expiryDate) {
}
