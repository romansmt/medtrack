package com.medtrack.application.service;

import com.medtrack.application.dto.AvailabilityItemResult;
import com.medtrack.application.dto.AvailabilityRequestItem;
import com.medtrack.application.dto.PharmacyAvailabilityResponse;
import com.medtrack.application.dto.PharmacyResponse;
import com.medtrack.application.port.PharmacyStockPort;
import com.medtrack.domain.Coordinates;
import com.medtrack.domain.Drug;
import com.medtrack.domain.PharmacyInventory;
import com.medtrack.infrastructure.persistence.DrugRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {

    private final PharmacyService pharmacyService;
    private final PharmacyStockPort pharmacyStockPort;
    private final DrugRepository drugRepository;
    private final PricingService pricingService;

    public AvailabilityService(PharmacyService pharmacyService, PharmacyStockPort pharmacyStockPort,
                                DrugRepository drugRepository, PricingService pricingService) {
        this.pharmacyService = pharmacyService;
        this.pharmacyStockPort = pharmacyStockPort;
        this.drugRepository = drugRepository;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public List<PharmacyAvailabilityResponse> checkAvailability(List<AvailabilityRequestItem> items,
                                                                  Coordinates origin, double radiusKm) {
        List<Long> drugIds = items.stream().map(AvailabilityRequestItem::drugId).toList();
        Map<Long, Drug> drugsById = drugRepository.findAllById(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, drug -> drug));

        if (drugsById.size() != drugIds.size()) {
            throw new NoSuchElementException("One or more requested drug ids do not exist");
        }

        Map<Long, Map<Long, PharmacyInventory>> inventoryByPharmacyThenDrug =
                pharmacyStockPort.findStockForDrugs(drugIds).stream()
                        .collect(Collectors.groupingBy(inventory -> inventory.getPharmacy().getId(),
                                Collectors.toMap(inventory -> inventory.getDrug().getId(), inventory -> inventory)));

        List<PharmacyResponse> nearbyPharmacies = pharmacyService.findNearby(origin, radiusKm);

        return nearbyPharmacies.stream()
                .map(pharmacy -> toAvailabilityResponse(pharmacy, items, drugsById,
                        inventoryByPharmacyThenDrug.getOrDefault(pharmacy.id(), Map.of())))
                .toList();
    }

    private PharmacyAvailabilityResponse toAvailabilityResponse(PharmacyResponse pharmacy,
                                                                  List<AvailabilityRequestItem> items,
                                                                  Map<Long, Drug> drugsById,
                                                                  Map<Long, PharmacyInventory> drugIdToInventory) {
        List<AvailabilityItemResult> itemResults = items.stream()
                .map(item -> toItemResult(item, drugsById.get(item.drugId()), drugIdToInventory.get(item.drugId())))
                .toList();

        boolean allAvailable = itemResults.stream().allMatch(AvailabilityItemResult::inStock);

        return new PharmacyAvailabilityResponse(pharmacy, allAvailable, itemResults);
    }

    private AvailabilityItemResult toItemResult(AvailabilityRequestItem item, Drug drug, PharmacyInventory inventory) {
        boolean inStock = inventory != null && inventory.isInStock();
        BigDecimal price = pricingService.getPrice(drug, inventory);

        return new AvailabilityItemResult(item.drugId(), drug.getName(), drug.getForm(), drug.getPackSize(),
                item.quantity(), inStock, price);
    }
}
