package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Doctor;
import com.medtrack.domain.Drug;
import com.medtrack.domain.Patient;
import com.medtrack.domain.Prescription;
import com.medtrack.domain.PrescriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PrescriptionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Test
    void findByPatientSvnrReturnsOnlyThatPatientsPrescriptions() {
        Patient patient = entityManager.persist(new Patient("9999999999", "Test Patient"));
        Doctor doctor = entityManager.persist(new Doctor("Dr. Test", "General Practitioner"));
        Drug drug = entityManager.persist(new Drug("TestDrug", "Test Substance"));
        entityManager.persist(new Prescription(patient, doctor, drug, "1 tablet daily",
                LocalDate.of(2026, 1, 1), PrescriptionStatus.OPEN));
        entityManager.flush();

        List<Prescription> found = prescriptionRepository.findByPatientSvnr("9999999999");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getDosage()).isEqualTo("1 tablet daily");
        assertThat(found.get(0).getStatus()).isEqualTo(PrescriptionStatus.OPEN);
    }

    @Test
    void findByPatientSvnrReturnsEmptyForUnknownSvnr() {
        assertThat(prescriptionRepository.findByPatientSvnr("0000000000")).isEmpty();
    }
}
