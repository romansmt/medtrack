package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.AccountAlreadyRegisteredException;
import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.IdAustriaRecord;
import com.medtrack.domain.IdAustriaRecordNotFoundException;
import com.medtrack.domain.InvalidCredentialsException;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.idaustria.IdAustriaRegistryRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@Component
public class MockEHealthCardAdapter implements EHealthCardPort {

    private static final int MEDTRACK_ID_SEED = 10_000_000;

    private final PatientRepository patientRepository;
    private final IdAustriaRegistryRepository registryRepository;
    private final PasswordEncoder passwordEncoder;

    public MockEHealthCardAdapter(
            PatientRepository patientRepository,
            IdAustriaRegistryRepository registryRepository,
            PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.registryRepository = registryRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EHealthCardSession lookupBySvnr(String svnr) {
        patientRepository.findBySvnr(svnr)
                .orElseThrow(() -> new PatientNotFoundException(svnr));
        return new EHealthCardSession(svnr, Instant.now());
    }

    @Override
    public Optional<Patient> findBySvnr(String svnr) {
        return patientRepository.findBySvnr(svnr);
    }

    @Override
    public ECardDetails scanCard(String fullName) {
        String[] nameParts = fullName.trim().split("\\s+", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";

        IdAustriaRecord record = registryRepository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)
                .orElseThrow(() -> new NoSuchElementException("No ID-Austria identity found matching name " + fullName));
        return toECardDetails(record);
    }

    @Override
    public Patient completeRegistration(String svnr, String email, String password) {
        IdAustriaRecord record = registryRepository.findBySvnr(svnr)
                .orElseThrow(() -> new IdAustriaRecordNotFoundException(svnr));

        Patient patient = patientRepository.findBySvnr(svnr).orElseGet(() -> {
            String fullName = record.getFirstName() + " " + record.getLastName();
            return new Patient(svnr, fullName, generateNextMedtrackId());
        });

        if (patient.hasAccount()) {
            throw new AccountAlreadyRegisteredException(svnr);
        }

        patient.setEmail(email);
        patient.setPasswordHash(passwordEncoder.encode(password));
        return patientRepository.save(patient);
    }

    @Override
    public Patient loginWithCredentials(String firstName, String lastName, String email, String rawPassword) {
        Patient patient = patientRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NoSuchElementException("No account found for email " + email));

        String[] nameParts = patient.getName().split(" ", 2);
        boolean nameMatches = nameParts[0].equalsIgnoreCase(firstName.trim())
                && (nameParts.length > 1 ? nameParts[1] : "").equalsIgnoreCase(lastName.trim());

        if (!nameMatches || !passwordEncoder.matches(rawPassword, patient.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return patient;
    }

    private static ECardDetails toECardDetails(IdAustriaRecord record) {
        return new ECardDetails(
                record.getSvnr(),
                null,
                record.getFirstName(),
                record.getLastName(),
                record.getDateOfBirth(),
                record.getCardSerialNumber(),
                record.getCarrierNumber(),
                record.getInsurerName(),
                record.getExpiryDate());
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
