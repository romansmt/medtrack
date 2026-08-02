package com.medtrack.application.port;

import com.medtrack.domain.Coordinates;

import java.util.Optional;

public interface GeoPort {

    Optional<Coordinates> geocode(String address);
}
