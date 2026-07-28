package com.medtrack.api;

import com.medtrack.application.dto.PriceComparisonEntry;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DrugControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void comparesAspirinPriceAcrossAllSeededPharmaciesCheapestFirst() {
        // Drug id 1 = Aspirin (OTC), stocked by all 8 seeded pharmacies. Central Vienna + 10km covers all of them.
        String url = "http://localhost:" + port + "/api/drugs/1/compare?lat=48.2082&lng=16.3719&radiusKm=10";

        ResponseEntity<PriceComparisonEntry[]> response = restTemplate.getForEntity(url, PriceComparisonEntry[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(8);

        PriceComparisonEntry cheapest = response.getBody()[0];
        PriceComparisonEntry priciest = response.getBody()[response.getBody().length - 1];

        assertThat(cheapest.pharmacy().name()).isEqualTo("VitaNova-Apotheke");
        assertThat(cheapest.price()).isEqualByComparingTo(new BigDecimal("4.30"));
        assertThat(priciest.pharmacy().name()).isEqualTo("Apotheke Zur Alten Muehle");
        assertThat(priciest.price()).isEqualByComparingTo(new BigDecimal("4.75"));

        // ascending order end-to-end, not just first/last
        for (int i = 1; i < response.getBody().length; i++) {
            assertThat(response.getBody()[i].price()).isGreaterThanOrEqualTo(response.getBody()[i - 1].price());
        }
    }

    @Test
    void returns404WhenComparingAnUnknownDrugId() {
        String url = "http://localhost:" + port + "/api/drugs/999999/compare?lat=48.2082&lng=16.3719";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
