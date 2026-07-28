package com.medtrack.application.service;

import com.medtrack.application.dto.ReservationResponse;
import com.medtrack.domain.Drug;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.domain.Pharmacy;
import com.medtrack.domain.Reservation;
import com.medtrack.domain.ReservationNotSupportedException;
import com.medtrack.domain.ReservationStatus;
import com.medtrack.infrastructure.persistence.DrugRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import com.medtrack.infrastructure.persistence.PharmacyRepository;
import com.medtrack.infrastructure.persistence.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final PatientRepository patientRepository;
    private final PharmacyRepository pharmacyRepository;
    private final DrugRepository drugRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(PatientRepository patientRepository, PharmacyRepository pharmacyRepository,
                               DrugRepository drugRepository, ReservationRepository reservationRepository) {
        this.patientRepository = patientRepository;
        this.pharmacyRepository = pharmacyRepository;
        this.drugRepository = drugRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> listReservations(String svnr) {
        Patient patient = findPatientOrThrow(svnr);
        return reservationRepository.findByPatientId(patient.getId()).stream()
                .map(ReservationService::toResponse)
                .toList();
    }

    @Transactional
    public ReservationResponse createReservation(String svnr, Long pharmacyId, Long drugId, int quantity) {
        Patient patient = findPatientOrThrow(svnr);
        Pharmacy pharmacy = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new NoSuchElementException("No pharmacy for id " + pharmacyId));
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new NoSuchElementException("No drug for id " + drugId));

        if (!pharmacy.isReservationSupported()) {
            throw new ReservationNotSupportedException(pharmacy.getName());
        }

        Reservation reservation = new Reservation(patient, pharmacy, drug, quantity, ReservationStatus.REQUESTED,
                Instant.now());
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public void cancelReservation(String svnr, Long reservationId) {
        Patient patient = findPatientOrThrow(svnr);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new NoSuchElementException("No reservation for id " + reservationId));

        // A reservation belonging to a different patient is treated as not found, not forbidden -
        // this endpoint shouldn't reveal that a reservation id exists for someone else.
        if (!reservation.getPatient().getId().equals(patient.getId())) {
            throw new NoSuchElementException("No reservation for id " + reservationId);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private Patient findPatientOrThrow(String svnr) {
        return patientRepository.findBySvnr(svnr)
                .orElseThrow(() -> new PatientNotFoundException(svnr));
    }

    private static ReservationResponse toResponse(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getPharmacy().getId(),
                reservation.getPharmacy().getName(), reservation.getDrug().getId(), reservation.getDrug().getName(),
                reservation.getQuantity(), reservation.getStatus().toString(), reservation.getRequestedAt());
    }
}
