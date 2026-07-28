package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.FavoritePharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoritePharmacyRepository extends JpaRepository<FavoritePharmacy, Long> {

    List<FavoritePharmacy> findByPatientId(Long patientId);

    boolean existsByPatientIdAndPharmacyId(Long patientId, Long pharmacyId);

    void deleteByPatientIdAndPharmacyId(Long patientId, Long pharmacyId);
}
