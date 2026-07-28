package com.medtrack.domain;

public class ReservationNotSupportedException extends RuntimeException {

    public ReservationNotSupportedException(String pharmacyName) {
        super("Pharmacy does not support reservations: " + pharmacyName);
    }
}
