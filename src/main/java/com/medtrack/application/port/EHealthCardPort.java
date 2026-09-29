package com.medtrack.application.port;

import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;

import java.time.LocalDate;

public interface EHealthCardPort {

    EHealthCardSession lookupBySvnr(String svnr);

    ECardDetails scanCard(String fullName);

    // Registers a brand-new patient (not one of the pre-seeded demo patients) and issues them an
    // e-card: a fresh SVNR (encoding the given birthdate, same as every other SVNR in this project)
    // and the next available MedTrack-ID. email is optional - only the standard (non-ID-Austria)
    // registration path sets it, since it's what standard login later looks the account up by.
    ECardDetails issueNewCard(String fullName, LocalDate dateOfBirth, String email);

    // Standard (non-ID-Austria) login: looks an existing patient up by the email they registered
    // with. No password check happens anywhere in this project - see RegistrationService/
    // RegistrationController for why that's a deliberate simplification, not an oversight.
    ECardDetails loginByEmail(String email);
}
