package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MockEHealthCardAdapterTest {

    @Mock
    private PatientRepository patientRepository;

    @Test
    void lookupBySvnrReturnsSessionWhenPatientExists() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(new Patient(svnr, "Anna Gruber")));

        MockEHealthCardAdapter adapter = new MockEHealthCardAdapter(patientRepository);

        assertThat(adapter.lookupBySvnr(svnr).svnr()).isEqualTo(svnr);
    }

    @Test
    void lookupBySvnrThrowsWhenPatientNotFound() {
        String svnr = "0000000000";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.empty());

        MockEHealthCardAdapter adapter = new MockEHealthCardAdapter(patientRepository);

        assertThatThrownBy(() -> adapter.lookupBySvnr(svnr))
                .isInstanceOf(PatientNotFoundException.class);
    }
}
