package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientAlreadyExistsException;
import com.medtrack.domain.PatientEmailAlreadyExistsException;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MockEHealthCardAdapter implements EHealthCardPort {

    // Fictional 4-digit carrier ("Kennnummer des Traegers") code - not a real OEGK identifier.
    private static final String CARRIER_NUMBER = "4711";
    private static final String CARRIER_NAME = "ÖGK";
    private static final int MEDTRACK_ID_SEED = 10_000_000;

    private final PatientRepository patientRepository;

    public MockEHealthCardAdapter(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Override
    public EHealthCardSession lookupBySvnr(String svnr) {
        patientRepository.findBySvnr(svnr)
                .orElseThrow(() -> new PatientNotFoundException(svnr));
        return new EHealthCardSession(svnr, Instant.now());
    }

    @Override
    public ECardDetails scanCard(String fullName) {
        Patient patient = patientRepository.findByNameIgnoreCase(fullName)
                .orElseThrow(() -> new NoSuchElementException("No demo patient found matching name " + fullName));
        return toECardDetails(patient);
    }

    @Override
    public ECardDetails issueNewCard(String fullName, LocalDate dateOfBirth, String email) {
        if (patientRepository.findByNameIgnoreCase(fullName).isPresent()) {
            throw new PatientAlreadyExistsException(fullName);
        }
        if (email != null && patientRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new PatientEmailAlreadyExistsException(email);
        }

        Patient patient = new Patient(generateUniqueSvnr(dateOfBirth), fullName, generateNextMedtrackId());
        patient.setEmail(email);
        return toECardDetails(patientRepository.save(patient));
    }

    @Override
    public ECardDetails loginByEmail(String email) {
        Patient patient = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("No account found for email " + email));
        return toECardDetails(patient);
    }

    private ECardDetails toECardDetails(Patient patient) {
        String svnr = patient.getSvnr();
        String[] nameParts = patient.getName().split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        return new ECardDetails(
                svnr,
                patient.getMedtrackId(),
                firstName,
                lastName,
                dateOfBirthFromSvnr(svnr),
                cardSerialNumberFor(svnr),
                CARRIER_NUMBER,
                CARRIER_NAME,
                LocalDate.now().plusYears(5).withMonth(12).withDayOfMonth(31));
    }

    // The last 6 digits of an Austrian SVNR are the holder's birthdate as ddMMyy - decode it the
    // same way the seed data (V2__seed_demo_data.sql) was constructed.
    private static LocalDate dateOfBirthFromSvnr(String svnr) {
        String ddmmyy = svnr.substring(4);
        int day = Integer.parseInt(ddmmyy.substring(0, 2));
        int month = Integer.parseInt(ddmmyy.substring(2, 4));
        int twoDigitYear = Integer.parseInt(ddmmyy.substring(4, 6));

        int currentYear = LocalDate.now().getYear();
        int fullYear = 2000 + twoDigitYear;
        if (fullYear > currentYear) {
            fullYear -= 100;
        }

        return LocalDate.of(fullYear, month, day);
    }

    // Deterministic 20-digit "Kennnummer der Karte" - not a real card number, just stable per svnr
    // so re-scanning the same demo patient's card always shows the same value.
    private static String cardSerialNumberFor(String svnr) {
        return svnr + new StringBuilder(svnr).reverse();
    }

    // A fresh SVNR for a newly registered patient: a random 4-digit sequence number, followed by
    // the given birthdate encoded ddMMyy - the same structure every other SVNR in this project
    // already has, so the birthdate-consistency check works identically for new accounts too.
    private String generateUniqueSvnr(LocalDate dateOfBirth) {
        String ddmmyy = String.format("%02d%02d%02d", dateOfBirth.getDayOfMonth(), dateOfBirth.getMonthValue(),
                dateOfBirth.getYear() % 100);

        for (int attempt = 0; attempt < 50; attempt++) {
            String sequence = String.format("%04d", ThreadLocalRandom.current().nextInt(1000, 10000));
            String candidate = sequence + ddmmyy;
            if (patientRepository.findBySvnr(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not generate a unique SVNR after 50 attempts");
    }

    // MedTrack-IDs are "MT-" + an 8-digit sequence, starting at 10000001 (see V9 seed values) -
    // the next one is always one past whatever the highest existing suffix is.
    private String generateNextMedtrackId() {
        int highestSuffix = patientRepository.findAll().stream()
                .map(Patient::getMedtrackId)
                .mapToInt(id -> Integer.parseInt(id.substring(3)))
                .max()
                .orElse(MEDTRACK_ID_SEED);
        return String.format("MT-%08d", highestSuffix + 1);
    }
}
