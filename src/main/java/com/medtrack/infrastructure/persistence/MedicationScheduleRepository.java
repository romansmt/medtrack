package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.MedicationSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationScheduleRepository extends JpaRepository<MedicationSchedule, Long> {

    List<MedicationSchedule> findByPatientId(Long patientId);

    List<MedicationSchedule> findByPatientIdAndActiveTrue(Long patientId);
}
