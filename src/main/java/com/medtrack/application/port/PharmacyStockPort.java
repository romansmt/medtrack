package com.medtrack.application.port;

import com.medtrack.domain.PharmacyInventory;

import java.util.List;

public interface PharmacyStockPort {

    List<PharmacyInventory> findStockForDrugs(List<Long> drugIds);

    List<PharmacyInventory> findStockForPharmacy(Long pharmacyId);
}
