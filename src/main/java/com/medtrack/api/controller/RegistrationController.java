package com.medtrack.api.controller;

import com.medtrack.application.dto.AccountResponse;
import com.medtrack.application.dto.CompleteRegistrationRequest;
import com.medtrack.application.dto.ECardDetailsResponse;
import com.medtrack.application.dto.IdAustriaLoginRequest;
import com.medtrack.application.dto.IdAustriaLoginResponse;
import com.medtrack.application.dto.ScanCardRequest;
import com.medtrack.application.dto.StandardLoginRequest;
import com.medtrack.application.dto.VerifyIdentityRequest;
import com.medtrack.application.dto.VerifyIdentityResponse;
import com.medtrack.application.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
@Tag(name = "Registration", description = "Registration is only possible through the mock ID Austria flow (login + e-card scan + "
        + "a real registry comparison), which ends with the user setting an email/password used for the separate "
        + "Standard (credentials) login afterward.")
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
    @Operation(summary = "Simulate scanning a photographed e-card - reads the ID-Austria registry by name")
    public ECardDetailsResponse scanCard(@RequestBody ScanCardRequest request) {
        return registrationService.scanCard(request.fullName());
    }

    @PostMapping("/verify-identity")
    @Operation(summary = "The real ID Austria comparison: looks the SVNR up in the mock registry and compares every "
            + "other field, reporting exactly what's wrong (wrong SVNR, or which fields mismatch) instead of "
            + "silently accepting anything")
    public VerifyIdentityResponse verifyIdentity(@RequestBody VerifyIdentityRequest request) {
        return registrationService.verifyIdentity(request);
    }

    @PostMapping("/complete-registration")
    @Operation(summary = "Only reachable after verify-identity succeeded - sets the email/password that becomes "
            + "this account's Standard-login credential. 409 if this identity already has an account.")
    public AccountResponse completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        return registrationService.completeRegistration(request);
    }

    @PostMapping("/standard-login")
    @Operation(summary = "Credential login: Name, Surname, Email and Password compared against the account created "
            + "during registration. 404 if no account exists for that email, 401 if the name/password don't match.")
    public AccountResponse standardLogin(@RequestBody StandardLoginRequest request) {
        return registrationService.loginStandard(request);
    }
}
