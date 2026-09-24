package com.medtrack.domain;

import java.time.LocalDate;

public record ECardDetails(
        String svnr,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String cardSerialNumber,
        String carrierNumber,
        String carrierName,
        LocalDate expiryDate) {
}
