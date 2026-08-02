package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.PharmacyOpeningHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PharmacyOpeningHoursRepository extends JpaRepository<PharmacyOpeningHours, Long> {

    List<PharmacyOpeningHours> findByPharmacyId(Long pharmacyId);

    List<PharmacyOpeningHours> findByPharmacyIdIn(List<Long> pharmacyIds);
}
