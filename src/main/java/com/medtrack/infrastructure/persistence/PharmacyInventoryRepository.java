package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.PharmacyInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PharmacyInventoryRepository extends JpaRepository<PharmacyInventory, Long> {

    List<PharmacyInventory> findByPharmacyId(Long pharmacyId);

    List<PharmacyInventory> findByDrugIdIn(List<Long> drugIds);

    Optional<PharmacyInventory> findByPharmacyIdAndDrugId(Long pharmacyId, Long drugId);
}
