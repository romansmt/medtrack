package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByPatientSvnr(String svnr);
}
