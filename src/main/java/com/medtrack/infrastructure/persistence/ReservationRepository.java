package com.medtrack.infrastructure.persistence;

import com.medtrack.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByPatientId(Long patientId);
}
