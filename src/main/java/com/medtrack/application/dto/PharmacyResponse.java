package com.medtrack.application.dto;

public record PharmacyResponse(
        Long id,
        String name,
        String address,
        double latitude,
        double longitude,
        boolean wheelchairAccessible,
        String phone,
        String email,
        String website,
        boolean reservationSupported,
        Double distanceKm,
        boolean openNow,
        boolean onCallNow) {
}
