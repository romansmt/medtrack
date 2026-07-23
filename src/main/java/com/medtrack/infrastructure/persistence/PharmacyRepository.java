package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Pharmacy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyRepository extends JpaRepository<Pharmacy, Long> {
}
