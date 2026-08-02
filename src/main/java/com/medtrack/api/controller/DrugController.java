package com.medtrack.api.controller;

import com.medtrack.application.dto.DrugLeafletResponse;
import com.medtrack.application.dto.DrugResponse;
import com.medtrack.application.dto.PriceComparisonEntry;
import com.medtrack.application.service.ComparisonService;
import com.medtrack.application.service.DrugService;
import com.medtrack.domain.Coordinates;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drugs")
@Tag(name = "Drugs", description = "Medication catalog search, detail, package leaflet, and price comparison")
public class DrugController {

    private static final double DEFAULT_RADIUS_KM = 5.0;

    private final DrugService drugService;
    private final ComparisonService comparisonService;

    public DrugController(DrugService drugService, ComparisonService comparisonService) {
        this.drugService = drugService;
        this.comparisonService = comparisonService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search the drug catalog by name, active substance, or PZN")
    public List<DrugResponse> search(@RequestParam String q) {
        return drugService.search(q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single drug catalog entry by id")
    public DrugResponse getById(@PathVariable Long id) {
        return drugService.getById(id);
    }

    @GetMapping("/{id}/leaflet")
    @Operation(summary = "Get the simulated package leaflet text for a drug")
    public DrugLeafletResponse getLeaflet(@PathVariable Long id) {
        return drugService.getLeaflet(id);
    }

    @GetMapping("/{id}/compare")
    @Operation(summary = "Compare a drug's price across nearby pharmacies, cheapest first")
    public List<PriceComparisonEntry> compare(@PathVariable Long id, @RequestParam double lat,
                                               @RequestParam double lng,
                                               @RequestParam(required = false) Double radiusKm) {
        return comparisonService.compare(id, new Coordinates(lat, lng),
                radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM);
    }
}
