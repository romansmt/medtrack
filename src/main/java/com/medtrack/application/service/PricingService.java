package com.medtrack.application.service;

import com.medtrack.domain.Drug;
import com.medtrack.domain.PharmacyInventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PricingService {

    // Fixed prescription co-payment (Rezeptgebuehr). Illustrative/fictional amount - not real,
    // current Austrian pricing - the point is modeling the fixed-fee-vs-OTC distinction, not the
    // exact euro figure.
    public static final BigDecimal REZEPTGEBUEHR = new BigDecimal("7.55");

    public BigDecimal getPrice(Drug drug, PharmacyInventory inventory) {
        if (inventory == null) {
            return null;
        }
        return drug.isPrescriptionRequired() ? REZEPTGEBUEHR : inventory.getPrice();
    }
}
