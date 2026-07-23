package com.medtrack.api.controller;

import com.medtrack.application.dto.DrugLeafletResponse;
import com.medtrack.application.dto.DrugResponse;
import com.medtrack.application.service.DrugService;
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
@Tag(name = "Drugs", description = "Medication catalog search, detail, and package leaflet")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
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
}
