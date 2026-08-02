package com.medtrack.application.service;

import com.medtrack.domain.Drug;
import com.medtrack.domain.Pharmacy;
import com.medtrack.domain.PharmacyInventory;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    private final Pharmacy pharmacy = new Pharmacy("Test Pharmacy", "Test Address", 48.2, 16.3,
            true, "+43 1 0000000", "test@example.example", null, true);

    @Test
    void returnsFlatRezeptgebuehrForPrescriptionRequiredDrug() {
        Drug drug = new Drug("Amoxicillin", "Amoxicillin", "Filmtabletten", "20 ST", "Sandoz GmbH",
                true, "99999901", null);
        PharmacyInventory inventory = new PharmacyInventory(pharmacy, drug, true, new BigDecimal("12.90"),
                Instant.now());

        assertThat(pricingService.getPrice(drug, inventory)).isEqualByComparingTo(PricingService.REZEPTGEBUEHR);
    }

    @Test
    void returnsInventoryPriceForOtcDrug() {
        Drug drug = new Drug("Aspirin", "Acetylsalicylic acid", "Tabletten", "30 ST", "Bayer Austria GmbH",
                false, "99999902", null);
        PharmacyInventory inventory = new PharmacyInventory(pharmacy, drug, true, new BigDecimal("4.50"),
                Instant.now());

        assertThat(pricingService.getPrice(drug, inventory)).isEqualByComparingTo(new BigDecimal("4.50"));
    }

    @Test
    void returnsNullWhenPharmacyDoesNotCarryTheDrugAtAll() {
        Drug drug = new Drug("Aspirin", "Acetylsalicylic acid", "Tabletten", "30 ST", "Bayer Austria GmbH",
                false, "99999903", null);

        assertThat(pricingService.getPrice(drug, null)).isNull();
    }
}
