package com.medtrack.api.controller;

import com.medtrack.application.dto.GeocodeResponse;
import com.medtrack.application.dto.PharmacyDetailResponse;
import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.application.service.PharmacyService;
import com.medtrack.domain.Coordinates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pharmacies")
@Tag(name = "Pharmacies", description = "Pharmacy locator: nearby search, on-call/emergency duty lookup, detail, geocoding")
public class PharmacyController {

    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/nearby")
    @Operation(summary = "Find pharmacies within a radius of a location, sorted by distance")
    public List<PharmacyResponse> findNearby(@RequestParam double lat, @RequestParam double lng,
                                              @RequestParam(required = false) Double radiusKm) {
        return pharmacyService.findNearby(new Coordinates(lat, lng),
                radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM);
    }

    @GetMapping("/on-call")
    @Operation(summary = "Find pharmacies currently on emergency duty (Bereitschaftsdienst), sorted by distance")
    public List<PharmacyResponse> findOnCallNow(@RequestParam double lat, @RequestParam double lng) {
        return pharmacyService.findOnCallNow(new Coordinates(lat, lng));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a pharmacy's detail, including its full weekly and on-call hours")
    public PharmacyDetailResponse getDetail(@PathVariable Long id) {
        return pharmacyService.getDetail(id);
    }

    @GetMapping("/geocode")
    @Operation(summary = "Resolve a free-text address to coordinates via the real Nominatim/OpenStreetMap API")
    public GeocodeResponse geocode(@RequestParam String address) {
        Coordinates coordinates = pharmacyService.geocode(address);
        return new GeocodeResponse(coordinates.latitude(), coordinates.longitude());
    }
}
