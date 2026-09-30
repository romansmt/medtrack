package com.medtrack.infrastructure.idaustria;

import com.medtrack.domain.ECardDetails;
import com.medtrack.domain.IdAustriaFieldMismatchException;
import com.medtrack.domain.IdAustriaRecord;
import com.medtrack.domain.IdAustriaRecordNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockIdAustriaAuthAdapterTest {

    @Mock
    private IdAustriaRegistryRepository registryRepository;

    private static final IdAustriaRecord ANNA = new IdAustriaRecord(
            "1234010190", "Anna", "Gruber", LocalDate.of(1990, 1, 1),
            "12340101900910104321", "4711", "ÖGK", LocalDate.of(2031, 12, 31));

    @Test
    void verifyFullIdentityThrowsWhenSvnrNotFound() {
        when(registryRepository.findBySvnr("0000000000")).thenReturn(Optional.empty());
        MockIdAustriaAuthAdapter adapter = new MockIdAustriaAuthAdapter(registryRepository);

        ECardDetails submitted = new ECardDetails(
                "0000000000", null, "Anna", "Gruber", LocalDate.of(1990, 1, 1),
                "12340101900910104321", "4711", "ÖGK", LocalDate.of(2031, 12, 31));

        assertThatThrownBy(() -> adapter.verifyFullIdentity(submitted))
                .isInstanceOf(IdAustriaRecordNotFoundException.class);
    }

    @Test
    void verifyFullIdentityListsEveryMismatchedField() {
        when(registryRepository.findBySvnr("1234010190")).thenReturn(Optional.of(ANNA));
        MockIdAustriaAuthAdapter adapter = new MockIdAustriaAuthAdapter(registryRepository);

        ECardDetails submitted = new ECardDetails(
                "1234010190", null, "Anna", "Wrong-Lastname", LocalDate.of(1990, 1, 1),
                "12340101900910104321", "4711", "ÖGK", LocalDate.of(2099, 1, 1));

        assertThatThrownBy(() -> adapter.verifyFullIdentity(submitted))
                .isInstanceOf(IdAustriaFieldMismatchException.class)
                .hasMessageContaining("Nachname")
                .hasMessageContaining("Ablaufdatum")
                .satisfies(ex -> assertThat(ex.getMessage()).doesNotContain("Vorname"));
    }

    @Test
    void verifyFullIdentitySucceedsWhenEverythingMatches() {
        when(registryRepository.findBySvnr("1234010190")).thenReturn(Optional.of(ANNA));
        MockIdAustriaAuthAdapter adapter = new MockIdAustriaAuthAdapter(registryRepository);

        ECardDetails submitted = new ECardDetails(
                "1234010190", null, "anna", "gruber", LocalDate.of(1990, 1, 1),
                "12340101900910104321", "4711", "ÖGK", LocalDate.of(2031, 12, 31));

        IdAustriaRecord result = adapter.verifyFullIdentity(submitted);

        assertThat(result.getSvnr()).isEqualTo("1234010190");
    }
}
