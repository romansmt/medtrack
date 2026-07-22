package com.medtrack.application.service;

import com.medtrack.application.dto.PrescriptionResponse;
import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.Doctor;
import com.medtrack.domain.Drug;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.domain.Prescription;
import com.medtrack.domain.PrescriptionStatus;
import com.medtrack.infrastructure.persistence.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private EHealthCardPort eHealthCardPort;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Test
    void getPrescriptionsForPatientMapsToDto() {
        String svnr = "1234010190";
        Patient patient = new Patient(svnr, "Anna Gruber");
        Doctor doctor = new Doctor("Dr. Julia Steiner", "General Practitioner");
        Drug drug = new Drug("Aspirin", "Acetylsalicylic acid");
        Prescription prescription = new Prescription(patient, doctor, drug, "1 tablet daily",
                LocalDate.of(2026, 6, 1), PrescriptionStatus.OPEN);

        when(eHealthCardPort.lookupBySvnr(svnr)).thenReturn(new EHealthCardSession(svnr, Instant.now()));
        when(prescriptionRepository.findByPatientSvnr(svnr)).thenReturn(List.of(prescription));

        PrescriptionService service = new PrescriptionService(eHealthCardPort, prescriptionRepository);
        List<PrescriptionResponse> result = service.getPrescriptionsForPatient(svnr);

        assertThat(result).containsExactly(
                new PrescriptionResponse("Aspirin", "1 tablet daily", "Dr. Julia Steiner",
                        LocalDate.of(2026, 6, 1), PrescriptionStatus.OPEN));
    }

    @Test
    void getPrescriptionsForPatientPropagatesNotFoundWithoutQueryingPrescriptions() {
        String svnr = "0000000000";
        when(eHealthCardPort.lookupBySvnr(svnr)).thenThrow(new PatientNotFoundException(svnr));

        PrescriptionService service = new PrescriptionService(eHealthCardPort, prescriptionRepository);

        assertThatThrownBy(() -> service.getPrescriptionsForPatient(svnr))
                .isInstanceOf(PatientNotFoundException.class);
        verify(prescriptionRepository, never()).findByPatientSvnr(svnr);
    }
}
