package com.medtrack.api.controller;

import com.medtrack.application.dto.AvailabilityCheckRequest;
import com.medtrack.application.dto.PharmacyAvailabilityResponse;
import com.medtrack.application.service.AvailabilityService;
import com.medtrack.domain.Coordinates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
@Tag(name = "Availability", description = "Cross-pharmacy availability check for a selection list of medications")
public class AvailabilityController {

    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @PostMapping("/check")
    @Operation(summary = "Check availability of one or more medications across nearby pharmacies")
    public List<PharmacyAvailabilityResponse> checkAvailability(@RequestBody AvailabilityCheckRequest request) {
        Coordinates origin = new Coordinates(request.latitude(), request.longitude());
        double radiusKm = request.radiusKm() != null ? request.radiusKm() : DEFAULT_RADIUS_KM;
        return availabilityService.checkAvailability(request.items(), origin, radiusKm);
    }
}
