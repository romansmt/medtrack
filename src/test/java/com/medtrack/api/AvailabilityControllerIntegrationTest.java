package com.medtrack.api;

import com.medtrack.application.dto.AvailabilityCheckRequest;
import com.medtrack.application.dto.AvailabilityRequestItem;
import com.medtrack.application.dto.PharmacyAvailabilityResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AvailabilityControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    // Exactly the seeded coordinates of "Apotheke Zum Goldenen Loewen", so it's the closest (distance 0) result.
    private static final double ORIGIN_LAT = 48.2082;
    private static final double ORIGIN_LNG = 16.3719;

    @Test
    void returnsAvailabilityAcrossNearbyPharmaciesForKnownDrug() {
        AvailabilityCheckRequest request = new AvailabilityCheckRequest(
                List.of(new AvailabilityRequestItem(1L, 2)), ORIGIN_LAT, ORIGIN_LNG, 10.0);

        ResponseEntity<PharmacyAvailabilityResponse[]> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/availability/check", request,
                PharmacyAvailabilityResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        // All 8 seeded pharmacies fall within a 10km radius of central Vienna.
        assertThat(response.getBody()).hasSize(8);

        PharmacyAvailabilityResponse closest = response.getBody()[0];
        assertThat(closest.pharmacy().name()).isEqualTo("Apotheke Zum Goldenen Loewen");
        assertThat(closest.pharmacy().distanceKm()).isEqualTo(0.0);
        assertThat(closest.items()).hasSize(1);
        assertThat(closest.items().get(0).drugId()).isEqualTo(1L);
        assertThat(closest.items().get(0).inStock()).isTrue();
        assertThat(closest.items().get(0).price()).isEqualByComparingTo(new BigDecimal("4.50"));
        assertThat(closest.allAvailable()).isTrue();
    }

    @Test
    void returns404WhenRequestReferencesAnUnknownDrugId() {
        AvailabilityCheckRequest request = new AvailabilityCheckRequest(
                List.of(new AvailabilityRequestItem(999_999L, 1)), ORIGIN_LAT, ORIGIN_LNG, 10.0);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/availability/check", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
