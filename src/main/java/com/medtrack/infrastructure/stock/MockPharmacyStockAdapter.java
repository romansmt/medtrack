package com.medtrack.infrastructure.stock;

import com.medtrack.application.port.PharmacyStockPort;
import com.medtrack.domain.PharmacyInventory;
import com.medtrack.infrastructure.persistence.PharmacyInventoryRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MockPharmacyStockAdapter implements PharmacyStockPort {

    private final PharmacyInventoryRepository pharmacyInventoryRepository;

    public MockPharmacyStockAdapter(PharmacyInventoryRepository pharmacyInventoryRepository) {
        this.pharmacyInventoryRepository = pharmacyInventoryRepository;
    }

    @Override
    public List<PharmacyInventory> findStockForDrugs(List<Long> drugIds) {
        return pharmacyInventoryRepository.findByDrugIdIn(drugIds);
    }

    @Override
    public List<PharmacyInventory> findStockForPharmacy(Long pharmacyId) {
        return pharmacyInventoryRepository.findByPharmacyId(pharmacyId);
    }
}
