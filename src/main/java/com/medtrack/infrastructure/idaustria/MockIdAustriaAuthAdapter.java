package com.medtrack.infrastructure.idaustria;

import com.medtrack.application.port.IdAustriaAuthPort;
import com.medtrack.domain.IdAustriaIdentity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

// Simulates ID Austria's federated login. A real integration would redirect to id.austria.gv.at
// and come back with a signed assertion; there is nothing to redirect to here, so this mock simply
// treats whatever name/birthdate the frontend collected as a successful assertion. It never fails -
// the actual identity check happens one step later, when the scanned e-card's SVNR-embedded
// birthdate is compared against what was asserted here (see RegistrationService).
@Component
public class MockIdAustriaAuthAdapter implements IdAustriaAuthPort {

    @Override
    public IdAustriaIdentity authenticate(String fullName, LocalDate dateOfBirth) {
        return new IdAustriaIdentity(fullName, dateOfBirth, Instant.now());
    }
}
