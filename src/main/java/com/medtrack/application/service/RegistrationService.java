package com.medtrack.application.service;

import com.medtrack.application.dto.AccountResponse;
import com.medtrack.application.dto.CompleteRegistrationRequest;
import com.medtrack.application.dto.ECardDetailsResponse;
import com.medtrack.application.dto.IdAustriaLoginResponse;
import com.medtrack.application.dto.StandardLoginRequest;
import com.medtrack.application.dto.VerifyIdentityRequest;
import com.medtrack.application.dto.VerifyIdentityResponse;
import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.application.port.IdAustriaAuthPort;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.IdAustriaIdentity;
import com.medtrack.domain.IdAustriaRecord;
import com.medtrack.domain.Patient;
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
        return toResponse(eHealthCardPort.scanCard(fullName), null);
    }

    // The real "call out to ID Austria and compare" step. Read-only - it never writes an account,
    // just tells the caller whether one already exists for this identity.
    @Transactional(readOnly = true)
    public VerifyIdentityResponse verifyIdentity(VerifyIdentityRequest request) {
        ECardDetails submitted = new ECardDetails(
                request.svnr(),
                null,
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.cardSerialNumber(),
                request.carrierNumber(),
                request.carrierName(),
                request.expiryDate());

        IdAustriaRecord verified = idAustriaAuthPort.verifyFullIdentity(submitted);
        Patient existing = eHealthCardPort.findBySvnr(verified.getSvnr()).orElse(null);

        String medtrackId = existing != null ? existing.getMedtrackId() : null;
        boolean alreadyRegistered = existing != null && existing.hasAccount();
        return new VerifyIdentityResponse(toResponse(verified, medtrackId), alreadyRegistered);
    }

    // Only reachable after a successful verifyIdentity - re-verifies the svnr server-side rather than
    // trusting the client's earlier call, then sets email+password on the (found-or-created) patient.
    @Transactional
    public AccountResponse completeRegistration(CompleteRegistrationRequest request) {
        Patient patient = eHealthCardPort.completeRegistration(request.svnr(), request.email(), request.password());
        return toAccountResponse(patient);
    }

    @Transactional(readOnly = true)
    public AccountResponse loginWithIdAustria(String svnr) {
        return toAccountResponse(eHealthCardPort.loginWithIdAustria(svnr));
    }

    @Transactional(readOnly = true)
    public AccountResponse loginStandard(StandardLoginRequest request) {
        Patient patient = eHealthCardPort.loginWithCredentials(
                request.firstName(), request.lastName(), request.email(), request.password());
        return toAccountResponse(patient);
    }

    private static ECardDetailsResponse toResponse(ECardDetails details, String medtrackId) {
        return new ECardDetailsResponse(
                details.svnr(),
                medtrackId != null ? medtrackId : details.medtrackId(),
                details.firstName(),
                details.lastName(),
                details.dateOfBirth(),
                details.cardSerialNumber(),
                details.carrierNumber(),
                details.carrierName(),
                details.expiryDate());
    }

    private static ECardDetailsResponse toResponse(IdAustriaRecord record, String medtrackId) {
        return new ECardDetailsResponse(
                record.getSvnr(),
                medtrackId,
                record.getFirstName(),
                record.getLastName(),
                record.getDateOfBirth(),
                record.getCardSerialNumber(),
                record.getCarrierNumber(),
                record.getInsurerName(),
                record.getExpiryDate());
    }

    private static AccountResponse toAccountResponse(Patient patient) {
        return new AccountResponse(patient.getSvnr(), patient.getMedtrackId(), patient.getName(), patient.getEmail());
    }
}
