package com.medtrack.api.controller;

import com.medtrack.application.dto.PrescriptionResponse;
import com.medtrack.application.service.PrescriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/oegk")
@Tag(name = "ÖGK prescriptions", description = "Look up a patient's prescriptions by SVNR")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/{svnr}/prescriptions")
    @Operation(summary = "Get a patient's prescriptions by SVNR")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Prescriptions found"),
            @ApiResponse(responseCode = "404", description = "No patient found for the given SVNR")
    })
    public List<PrescriptionResponse> getPrescriptions(@PathVariable String svnr) {
        return prescriptionService.getPrescriptionsForPatient(svnr);
    }
}
