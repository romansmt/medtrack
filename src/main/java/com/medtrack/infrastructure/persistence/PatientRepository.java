package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findBySvnr(String svnr);
}
