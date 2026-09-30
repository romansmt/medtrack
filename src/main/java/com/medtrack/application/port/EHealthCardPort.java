package com.medtrack.application.port;

import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.Patient;

import java.util.Optional;

public interface EHealthCardPort {

    EHealthCardSession lookupBySvnr(String svnr);

    // Looks up MedTrack's own Patient row for a given SVNR, if one exists yet (it may, even before
    // an account/password is set - see the seeded demo patients). Used to tell whether an
    // ID-Austria-verified identity already has a MedTrack account (Patient.hasAccount()) and to
    // surface its existing MedTrack-ID during the confirm step.
    Optional<Patient> findBySvnr(String svnr);

    // Simulates photographing an e-card: reads identity data from the ID-Austria registry (the
    // government-owned source), not from MedTrack's own patient table - scanning your card doesn't
    // require already having a MedTrack account.
    ECardDetails scanCard(String fullName);

    // Finishes registration for an identity that has already passed IdAustriaAuthPort.verifyFullIdentity:
    // finds (or creates, using the registry's own data - never an invented SVNR) the matching Patient
    // row and sets email + a hashed password on it. Throws AccountAlreadyRegisteredException if that
    // Patient already has a password set.
    Patient completeRegistration(String svnr, String email, String password);

    // "Mit ID Austria" login for an already-registered account: re-verifies the SVNR still resolves
    // in the registry, then looks up the matching Patient. Throws java.util.NoSuchElementException if
    // no Patient row exists yet, or it exists but has no password set (not registered yet).
    Patient loginWithIdAustria(String svnr);

    // Standard (non-ID-Austria) login: looks a Patient up by email, then checks the submitted
    // name/surname and password against the stored record. Throws java.util.NoSuchElementException
    // if the email is unknown ("no such user"), or InvalidCredentialsException if the name or
    // password don't match.
    Patient loginWithCredentials(String firstName, String lastName, String email, String rawPassword);
}
