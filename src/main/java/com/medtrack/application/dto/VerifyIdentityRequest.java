package com.medtrack.application.dto;

import java.time.LocalDate;

// Everything ConfirmCardDataStep collects on the frontend - submitted for a real comparison against
// the (mock) ID-Austria registry. carrierName carries "Versicherung".
public record VerifyIdentityRequest(
        String svnr,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String cardSerialNumber,
        String carrierNumber,
        String carrierName,
        LocalDate expiryDate) {
}
