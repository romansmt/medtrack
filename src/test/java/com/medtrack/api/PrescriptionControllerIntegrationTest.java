package com.medtrack.api;

import com.medtrack.application.dto.PrescriptionResponse;
import com.medtrack.domain.PrescriptionStatus;
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

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PrescriptionControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Test
    void returnsSeededPrescriptionsForKnownSvnr() {
        ResponseEntity<PrescriptionResponse[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/oegk/1234010190/prescriptions", PrescriptionResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyInAnyOrder(
                new PrescriptionResponse("Aspirin", "1 tablet daily", "Dr. Julia Steiner",
                        LocalDate.of(2026, 6, 1), PrescriptionStatus.OPEN),
                new PrescriptionResponse("Paracetamol", "500mg every 6 hours as needed", "Dr. Marie Huber",
                        LocalDate.of(2026, 6, 10), PrescriptionStatus.REDEEMED));
    }

    @Test
    void returns404ForUnknownSvnr() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/oegk/0000000000/prescriptions", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
