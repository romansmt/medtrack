package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Component
public class MockEHealthCardAdapter implements EHealthCardPort {

    // Fictional 4-digit carrier ("Kennnummer des Traegers") code - not a real OEGK identifier.
    private static final String CARRIER_NUMBER = "4711";
    private static final String CARRIER_NAME = "ÖGK";

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
}
