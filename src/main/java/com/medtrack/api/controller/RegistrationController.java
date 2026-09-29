package com.medtrack.api.controller;

import com.medtrack.application.dto.ECardDetailsResponse;
import com.medtrack.application.dto.IdAustriaLoginRequest;
import com.medtrack.application.dto.IdAustriaLoginResponse;
import com.medtrack.application.dto.ScanCardRequest;
import com.medtrack.application.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@Tag(name = "Registration", description = "One-time onboarding: mock ID Austria login, then an e-card scan to establish the patient's SVNR")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/id-austria-login")
    @Operation(summary = "Simulate an ID Austria login - always succeeds, asserts the given identity")
    public IdAustriaLoginResponse loginWithIdAustria(@RequestBody IdAustriaLoginRequest request) {
        return registrationService.loginWithIdAustria(request.fullName(), request.dateOfBirth());
    }

    @PostMapping("/scan-card")
    @Operation(summary = "Simulate scanning a photographed e-card for the given (ID Austria-asserted) full name")
    public ECardDetailsResponse scanCard(@RequestBody ScanCardRequest request) {
        return registrationService.scanCard(request.fullName());
    }

    @PostMapping("/create-account")
    @Operation(summary = "Register a brand-new MedTrack account not tied to one of the pre-seeded demo patients - "
            + "issues a fresh SVNR, e-card, and MedTrack-ID")
    public ECardDetailsResponse createAccount(@RequestBody IdAustriaLoginRequest request) {
        return registrationService.createAccount(request.fullName(), request.dateOfBirth());
    }
}
