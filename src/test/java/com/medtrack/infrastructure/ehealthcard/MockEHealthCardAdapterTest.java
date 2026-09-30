package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.domain.AccountAlreadyRegisteredException;
import com.medtrack.domain.IdAustriaRecord;
import com.medtrack.domain.IdAustriaRecordNotFoundException;
import com.medtrack.domain.InvalidCredentialsException;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.idaustria.IdAustriaRegistryRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockEHealthCardAdapterTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private IdAustriaRegistryRepository registryRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private MockEHealthCardAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new MockEHealthCardAdapter(patientRepository, registryRepository, passwordEncoder);
    }

    @Test
    void lookupBySvnrReturnsSessionWhenPatientExists() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(new Patient(svnr, "Anna Gruber", "MT-00000000")));

        assertThat(adapter.lookupBySvnr(svnr).svnr()).isEqualTo(svnr);
    }

    @Test
    void lookupBySvnrThrowsWhenPatientNotFound() {
        String svnr = "0000000000";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.lookupBySvnr(svnr))
                .isInstanceOf(PatientNotFoundException.class);
    }

    @Test
    void completeRegistrationThrowsWhenAlreadyRegistered() {
        String svnr = "1234010190";
        Patient existing = new Patient(svnr, "Anna Gruber", "MT-10000001");
        existing.setEmail("anna.gruber@demo.medtrack.local");
        existing.setPasswordHash("already-set");

        when(registryRepository.findBySvnr(svnr)).thenReturn(Optional.of(anna()));
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> adapter.completeRegistration(svnr, "new@example.com", "Secret123!"))
                .isInstanceOf(AccountAlreadyRegisteredException.class);
    }

    @Test
    void completeRegistrationThrowsWhenSvnrNotInRegistry() {
        String svnr = "0000000000";
        when(registryRepository.findBySvnr(svnr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.completeRegistration(svnr, "new@example.com", "Secret123!"))
                .isInstanceOf(IdAustriaRecordNotFoundException.class);
    }

    @Test
    void completeRegistrationSetsHashedPasswordOnFreshPatient() {
        String svnr = "4567040475";
        when(registryRepository.findBySvnr(svnr)).thenReturn(Optional.of(paul()));
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(new Patient(svnr, "Paul Wagner", "MT-10000004")));
        when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Patient result = adapter.completeRegistration(svnr, "paul.wagner@example.com", "Secret123!");

        assertThat(result.getEmail()).isEqualTo("paul.wagner@example.com");
        assertThat(passwordEncoder.matches("Secret123!", result.getPasswordHash())).isTrue();
    }

    @Test
    void loginWithCredentialsThrowsWhenEmailUnknown() {
        when(patientRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.loginWithCredentials("Anna", "Gruber", "unknown@example.com", "whatever"))
                .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void loginWithCredentialsThrowsWhenPasswordWrong() {
        Patient patient = new Patient("1234010190", "Anna Gruber", "MT-10000001");
        patient.setEmail("anna.gruber@demo.medtrack.local");
        patient.setPasswordHash(passwordEncoder.encode("MedTrack2026!"));
        when(patientRepository.findByEmailIgnoreCase("anna.gruber@demo.medtrack.local")).thenReturn(Optional.of(patient));

        assertThatThrownBy(() -> adapter.loginWithCredentials("Anna", "Gruber", "anna.gruber@demo.medtrack.local", "wrong"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginWithCredentialsSucceedsWithMatchingNameAndPassword() {
        Patient patient = new Patient("1234010190", "Anna Gruber", "MT-10000001");
        patient.setEmail("anna.gruber@demo.medtrack.local");
        patient.setPasswordHash(passwordEncoder.encode("MedTrack2026!"));
        when(patientRepository.findByEmailIgnoreCase("anna.gruber@demo.medtrack.local")).thenReturn(Optional.of(patient));

        Patient result = adapter.loginWithCredentials("Anna", "Gruber", "anna.gruber@demo.medtrack.local", "MedTrack2026!");

        assertThat(result.getSvnr()).isEqualTo("1234010190");
    }

    private static IdAustriaRecord anna() {
        return new IdAustriaRecord(
                "1234010190", "Anna", "Gruber", LocalDate.of(1990, 1, 1),
                "12340101900910104321", "4711", "ÖGK", LocalDate.of(2031, 12, 31));
    }

    private static IdAustriaRecord paul() {
        return new IdAustriaRecord(
                "4567040475", "Paul", "Wagner", LocalDate.of(1975, 4, 4),
                "45670404755740407654", "4711", "ÖGK", LocalDate.of(2031, 12, 31));
    }
}
