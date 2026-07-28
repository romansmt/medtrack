package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.MedicationScheduleTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationScheduleTimeRepository extends JpaRepository<MedicationScheduleTime, Long> {

    List<MedicationScheduleTime> findByScheduleId(Long scheduleId);

    List<MedicationScheduleTime> findByScheduleIdIn(List<Long> scheduleIds);
}
