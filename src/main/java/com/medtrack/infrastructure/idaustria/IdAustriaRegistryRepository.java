package com.medtrack.infrastructure.idaustria;

import com.medtrack.domain.IdAustriaRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Deliberately package-private in spirit (not enforced by Java, but by convention): only
// MockIdAustriaAuthAdapter and MockEHealthCardAdapter (simulating a call out to ID Austria) should
// use this - no other service reaches into "ID Austria's own database" directly.
public interface IdAustriaRegistryRepository extends JpaRepository<IdAustriaRecord, String> {

    Optional<IdAustriaRecord> findBySvnr(String svnr);

    Optional<IdAustriaRecord> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);
}
