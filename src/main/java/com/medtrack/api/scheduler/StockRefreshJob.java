package com.medtrack.api.scheduler;

import com.medtrack.domain.PharmacyInventory;
import com.medtrack.infrastructure.persistence.PharmacyInventoryRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a live per-pharmacy stock feed (the architecture doc's "stock refresh" cross-cutting
 * concern) by periodically jittering a sample of inventory rows and touching last_updated, so the
 * UI's "last updated" freshness line moves over time the way a real polled integration would.
 */
@Component
public class StockRefreshJob {

    private final PharmacyInventoryRepository pharmacyInventoryRepository;

    public StockRefreshJob(PharmacyInventoryRepository pharmacyInventoryRepository) {
        this.pharmacyInventoryRepository = pharmacyInventoryRepository;
    }

    @Scheduled(fixedRate = 60_000)
    public void refresh() {
        List<PharmacyInventory> all = pharmacyInventoryRepository.findAll();
        if (all.isEmpty()) {
            return;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int sampleSize = Math.max(1, all.size() / 10);
        List<PharmacyInventory> touched = new ArrayList<>();

        for (int i = 0; i < sampleSize; i++) {
            PharmacyInventory row = all.get(random.nextInt(all.size()));
            if (random.nextInt(20) == 0) {
                row.setInStock(!row.isInStock());
            }
            row.setLastUpdated(Instant.now());
            touched.add(row);
        }

        pharmacyInventoryRepository.saveAll(touched);
    }
}
