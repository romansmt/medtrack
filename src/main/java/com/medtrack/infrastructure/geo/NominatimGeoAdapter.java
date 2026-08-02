package com.medtrack.infrastructure.geo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.medtrack.application.port.GeoPort;
import com.medtrack.domain.Coordinates;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

/**
 * Real integration, not mocked: calls the public Nominatim/OpenStreetMap geocoding API to resolve a
 * free-text address into coordinates, for the manual "set location" search box (as opposed to
 * browser geolocation, which the frontend uses directly for "use my location"). Per Nominatim's
 * usage policy, requests carry an identifying User-Agent (no personal data) and are only issued on
 * explicit user submission, never per keystroke.
 */
@Component
public class NominatimGeoAdapter implements GeoPort {

    private static final String USER_AGENT =
            "MedTrack-StudyProject/0.1 (student portfolio project; no real personal data)";

    private final RestClient restClient;

    public NominatimGeoAdapter() {
        this.restClient = RestClient.builder()
                .baseUrl("https://nominatim.openstreetmap.org")
                .defaultHeader(HttpHeaders.USER_AGENT, USER_AGENT)
                .build();
    }

    @Override
    public Optional<Coordinates> geocode(String address) {
        List<NominatimResult> results = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", address)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<NominatimResult>>() {
                });

        if (results == null || results.isEmpty()) {
            return Optional.empty();
        }

        NominatimResult first = results.get(0);
        return Optional.of(new Coordinates(Double.parseDouble(first.lat()), Double.parseDouble(first.lon())));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NominatimResult(String lat, String lon) {
    }
}
