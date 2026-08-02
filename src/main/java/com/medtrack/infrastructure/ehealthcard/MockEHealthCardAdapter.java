package com.medtrack.infrastructure.ehealthcard;

import com.medtrack.application.port.EHealthCardPort;
import com.medtrack.domain.EHealthCardSession;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class MockEHealthCardAdapter implements EHealthCardPort {

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
}
