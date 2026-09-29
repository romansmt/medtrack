package com.medtrack.application.port;

import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;

import java.time.LocalDate;

public interface EHealthCardPort {

    EHealthCardSession lookupBySvnr(String svnr);

    ECardDetails scanCard(String fullName);

    // Registers a brand-new patient (not one of the pre-seeded demo patients) and issues them an
    // e-card: a fresh SVNR (encoding the given birthdate, same as every other SVNR in this project)
    // and the next available MedTrack-ID.
    ECardDetails issueNewCard(String fullName, LocalDate dateOfBirth);
}
