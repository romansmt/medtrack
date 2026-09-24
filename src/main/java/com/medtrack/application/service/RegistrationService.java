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
        ECardDetails details = eHealthCardPort.scanCard(fullName);
        return new ECardDetailsResponse(
                details.svnr(),
                details.firstName(),
                details.lastName(),
                details.dateOfBirth(),
                details.cardSerialNumber(),
                details.carrierNumber(),
                details.carrierName(),
                details.expiryDate());
    }
}
