package com.medtrack.application.port;

import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.IdAustriaIdentity;
import com.medtrack.domain.IdAustriaRecord;

import java.time.LocalDate;

public interface IdAustriaAuthPort {

    // Lightweight, always-succeeding assertion of a name+birthdate - just carries the claimed
    // identity into the rest of the registration/login wizard. The real check is verifyFullIdentity.
    IdAustriaIdentity authenticate(String fullName, LocalDate dateOfBirth);

    // Simulates the actual "call out to ID Austria and compare" step: looks the submission up by
    // SVNR in the (mock) ID-Austria registry and compares every other field. Throws
    // IdAustriaRecordNotFoundException if the SVNR itself doesn't exist, or
    // IdAustriaFieldMismatchException (listing every mismatched field) if the SVNR exists but other
    // fields don't match.
    IdAustriaRecord verifyFullIdentity(ECardDetails submitted);
}
