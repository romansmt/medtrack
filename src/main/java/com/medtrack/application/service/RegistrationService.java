package com.medtrack.application.service;

import com.medtrack.application.dto.ECardDetailsResponse;
import com.medtrack.application.dto.IdAustriaLoginResponse;
import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.application.port.IdAustriaAuthPort;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.IdAustriaIdentity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RegistrationService {

    private final IdAustriaAuthPort idAustriaAuthPort;
    private final EHealthCardPort eHealthCardPort;

    public RegistrationService(IdAustriaAuthPort idAustriaAuthPort, EHealthCardPort eHealthCardPort) {
        this.idAustriaAuthPort = idAustriaAuthPort;
        this.eHealthCardPort = eHealthCardPort;
    }

    public IdAustriaLoginResponse loginWithIdAustria(String fullName, LocalDate dateOfBirth) {
        IdAustriaIdentity identity = idAustriaAuthPort.authenticate(fullName, dateOfBirth);
        return new IdAustriaLoginResponse(identity.fullName(), identity.dateOfBirth(), identity.authenticatedAt());
    }

    @Transactional(readOnly = true)
    public ECardDetailsResponse scanCard(String fullName) {
        return toResponse(eHealthCardPort.scanCard(fullName));
    }

    // Unlike scanCard/loginWithIdAustria, this writes a new Patient row - not read-only.
    @Transactional
    public ECardDetailsResponse createAccount(String fullName, LocalDate dateOfBirth) {
        return toResponse(eHealthCardPort.issueNewCard(fullName, dateOfBirth));
    }

    private static ECardDetailsResponse toResponse(ECardDetails details) {
        return new ECardDetailsResponse(
                details.svnr(),
                details.medtrackId(),
                details.firstName(),
                details.lastName(),
                details.dateOfBirth(),
                details.cardSerialNumber(),
                details.carrierNumber(),
                details.carrierName(),
                details.expiryDate());
    }
}
