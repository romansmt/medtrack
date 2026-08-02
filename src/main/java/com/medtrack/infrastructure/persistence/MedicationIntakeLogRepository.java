package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.MedicationIntakeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicationIntakeLogRepository extends JpaRepository<MedicationIntakeLog, Long> {

    Optional<MedicationIntakeLog> findByScheduleTimeIdAndScheduledDate(Long scheduleTimeId, LocalDate scheduledDate);

    List<MedicationIntakeLog> findByScheduleTimeIdInAndScheduledDate(List<Long> scheduleTimeIds,
                                                                      LocalDate scheduledDate);
}
