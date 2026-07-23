package com.medtrack.api.controller;

import com.medtrack.application.dto.PatientResponse;
import com.medtrack.application.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@Tag(name = "Patients", description = "Demo patient directory - stands in for real login while auth is out of scope")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "List demo patients, for the frontend's patient selector")
    public List<PatientResponse> listPatients() {
        return patientService.listAll();
    }
}
