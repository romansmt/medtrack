package com.medtrack.infrastructure.config;

import com.medtrack.domain.Patient;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

// Fills in email+password for all 4 seeded demo patients, so every one of them is an "already
// registered" account that can log in immediately via the single Standard (name/surname/email/
// password) login method - there's no other way to log in, so a seeded patient without credentials
// would simply be unreachable. Runs as an ApplicationRunner (not a Flyway SQL literal) because the
// hash has to come from the app's own PasswordEncoder - there's no portable way to precompute a
// BCrypt string in plain SQL. Idempotent: only fills a row whose password_hash is still null, so it
// never overwrites a password someone has actually set, and does nothing once all 4 rows are filled.
@Component
public class DemoAccountSeeder implements ApplicationRunner {

    // Shared across all four - documented publicly in docs/TEST_CREDENTIALS.md, same "fake and
    // intentionally public" spirit as the admin access code (see MockAdminAuthAdapter).
    static final String DEMO_PASSWORD = "MedTrack2026!";

    private static final Map<String, String> DEMO_EMAILS_BY_SVNR = Map.of(
            "1234010190", "anna.gruber@demo.medtrack.local",
            "2345020285", "max.bauer@demo.medtrack.local",
            "3456030380", "lena.hofer@demo.medtrack.local",
            "4567040475", "paul.wagner@demo.medtrack.local");

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoAccountSeeder(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        DEMO_EMAILS_BY_SVNR.forEach((svnr, email) -> patientRepository.findBySvnr(svnr).ifPresent(patient -> {
            if (!patient.hasAccount()) {
                seedCredentials(patient, email);
            }
        }));
    }

    private void seedCredentials(Patient patient, String email) {
        patient.setEmail(email);
        patient.setPasswordHash(passwordEncoder.encode(DEMO_PASSWORD));
        patientRepository.save(patient);
    }
}
