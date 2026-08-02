package com.medtrack.application.service;

import com.medtrack.application.dto.PrescriptionResponse;
import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.Prescription;
import com.medtrack.infrastructure.persistence.PrescriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrescriptionService {

    private final EHealthCardPort eHealthCardPort;
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(EHealthCardPort eHealthCardPort, PrescriptionRepository prescriptionRepository) {
        this.eHealthCardPort = eHealthCardPort;
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getPrescriptionsForPatient(String svnr) {
        eHealthCardPort.lookupBySvnr(svnr);

        return prescriptionRepository.findByPatientSvnr(svnr).stream()
                .map(PrescriptionService::toResponse)
                .toList();
    }

    private static PrescriptionResponse toResponse(Prescription prescription) {
        return new PrescriptionResponse(
                prescription.getDrug().getName(),
                prescription.getDosage(),
                prescription.getDoctor().getName(),
                prescription.getIssuedDate(),
                prescription.getStatus());
    }
}
