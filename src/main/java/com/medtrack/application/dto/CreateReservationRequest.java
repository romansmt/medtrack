package com.medtrack.application.dto;

public record CreateReservationRequest(Long pharmacyId, Long drugId, int quantity) {
}
