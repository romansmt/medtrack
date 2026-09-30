package com.medtrack.infrastructure.idaustria;

import com.medtrack.application.port.IdAustriaAuthPort;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.IdAustriaFieldMismatchException;
import com.medtrack.domain.IdAustriaIdentity;
import com.medtrack.domain.IdAustriaRecord;
import com.medtrack.domain.IdAustriaRecordNotFoundException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Simulates ID Austria's federated login and the identity-comparison API a real integration would
// call. authenticate() never fails - it's just the "who are you claiming to be" step that kicks off
// the wizard. verifyFullIdentity() is where ID Austria (not MedTrack) actually compares the
// submitted data against its own record and reports exactly what doesn't match.
@Component
public class MockIdAustriaAuthAdapter implements IdAustriaAuthPort {

    private final IdAustriaRegistryRepository registryRepository;

    public MockIdAustriaAuthAdapter(IdAustriaRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    @Override
    public IdAustriaIdentity authenticate(String fullName, LocalDate dateOfBirth) {
        return new IdAustriaIdentity(fullName, dateOfBirth, Instant.now());
    }

    @Override
    public IdAustriaRecord verifyFullIdentity(ECardDetails submitted) {
        IdAustriaRecord record = registryRepository.findBySvnr(submitted.svnr())
                .orElseThrow(() -> new IdAustriaRecordNotFoundException(submitted.svnr()));

        List<String> mismatches = new ArrayList<>();
        if (!equalsIgnoreCaseTrim(submitted.firstName(), record.getFirstName())) {
            mismatches.add("Vorname");
        }
        if (!equalsIgnoreCaseTrim(submitted.lastName(), record.getLastName())) {
            mismatches.add("Nachname");
        }
        if (!Objects.equals(submitted.dateOfBirth(), record.getDateOfBirth())) {
            mismatches.add("Geburtsdatum");
        }
        if (!equalsIgnoreCaseTrim(submitted.cardSerialNumber(), record.getCardSerialNumber())) {
            mismatches.add("Kennnummer der Karte");
        }
        if (!equalsIgnoreCaseTrim(submitted.carrierNumber(), record.getCarrierNumber())) {
            mismatches.add("Kennnummer des Trägers");
        }
        if (!equalsIgnoreCaseTrim(submitted.carrierName(), record.getInsurerName())) {
            mismatches.add("Versicherung");
        }
        if (!Objects.equals(submitted.expiryDate(), record.getExpiryDate())) {
            mismatches.add("Ablaufdatum");
        }

        if (!mismatches.isEmpty()) {
            throw new IdAustriaFieldMismatchException(mismatches);
        }
        return record;
    }

    private static boolean equalsIgnoreCaseTrim(String a, String b) {
        return a != null && b != null && a.trim().equalsIgnoreCase(b.trim());
    }
}
