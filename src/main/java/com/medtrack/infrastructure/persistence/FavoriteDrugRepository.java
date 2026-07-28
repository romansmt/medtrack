package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.FavoriteDrug;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteDrugRepository extends JpaRepository<FavoriteDrug, Long> {

    List<FavoriteDrug> findByPatientId(Long patientId);

    boolean existsByPatientIdAndDrugId(Long patientId, Long drugId);

    void deleteByPatientIdAndDrugId(Long patientId, Long drugId);
}
