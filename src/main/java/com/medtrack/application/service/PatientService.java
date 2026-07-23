package com.medtrack.application.service;

import com.medtrack.application.dto.PatientResponse;
import com.medtrack.domain.Patient;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> listAll() {
        return patientRepository.findAll().stream()
                .map(PatientService::toResponse)
                .toList();
    }

    private static PatientResponse toResponse(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getSvnr(), patient.getName());
    }
}
