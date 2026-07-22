package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
