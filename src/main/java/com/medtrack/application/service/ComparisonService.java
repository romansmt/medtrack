package com.medtrack.application.service;

import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.application.dto.PriceComparisonEntry;
import com.medtrack.application.port.PharmacyStockPort;
import com.medtrack.domain.Coordinates;
import com.medtrack.domain.Drug;
import com.medtrack.domain.PharmacyInventory;
import com.medtrack.infrastructure.persistence.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ComparisonService {

    private final PharmacyService pharmacyService;
    private final PharmacyStockPort pharmacyStockPort;
    private final DrugRepository drugRepository;
    private final PricingService pricingService;

    public ComparisonService(PharmacyService pharmacyService, PharmacyStockPort pharmacyStockPort,
                              DrugRepository drugRepository, PricingService pricingService) {
        this.pharmacyService = pharmacyService;
        this.pharmacyStockPort = pharmacyStockPort;
        this.drugRepository = drugRepository;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public List<PriceComparisonEntry> compare(Long drugId, Coordinates origin, double radiusKm) {
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new NoSuchElementException("No drug for id " + drugId));

        Map<Long, PharmacyInventory> inventoryByPharmacy = pharmacyStockPort.findStockForDrugs(List.of(drugId))
                .stream()
                .collect(Collectors.toMap(inventory -> inventory.getPharmacy().getId(), inventory -> inventory));

        return pharmacyService.findNearby(origin, radiusKm).stream()
                .map(pharmacy -> toEntry(pharmacy, drug, inventoryByPharmacy.get(pharmacy.id())))
                .filter(entry -> entry.price() != null)
                .sorted(Comparator.comparing(PriceComparisonEntry::price))
                .toList();
    }

    private PriceComparisonEntry toEntry(PharmacyResponse pharmacy, Drug drug, PharmacyInventory inventory) {
        boolean inStock = inventory != null && inventory.isInStock();
        return new PriceComparisonEntry(pharmacy, pricingService.getPrice(drug, inventory), inStock);
    }
}
