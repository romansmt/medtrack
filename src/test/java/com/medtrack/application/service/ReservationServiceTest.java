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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private PharmacyRepository pharmacyRepository;
    @Mock
    private DrugRepository drugRepository;
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private Patient patient;
    @Mock
    private Patient otherPatient;
    @Mock
    private Pharmacy pharmacy;
    @Mock
    private Drug drug;

    private ReservationService newService() {
        return new ReservationService(patientRepository, pharmacyRepository, drugRepository, reservationRepository);
    }

    @Test
    void createReservationSucceedsWhenPharmacySupportsReservations() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(pharmacyRepository.findById(5L)).thenReturn(Optional.of(pharmacy));
        when(drugRepository.findById(3L)).thenReturn(Optional.of(drug));
        when(pharmacy.isReservationSupported()).thenReturn(true);
        when(pharmacy.getId()).thenReturn(5L);
        when(pharmacy.getName()).thenReturn("Test Pharmacy");
        when(drug.getId()).thenReturn(3L);
        when(drug.getName()).thenReturn("Test Drug");
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReservationResponse response = newService().createReservation(svnr, 5L, 3L, 2);

        assertThat(response.pharmacyName()).isEqualTo("Test Pharmacy");
        assertThat(response.drugName()).isEqualTo("Test Drug");
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.status()).isEqualTo("REQUESTED");
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservationRejectsPharmacyThatDoesNotSupportReservations() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(pharmacyRepository.findById(5L)).thenReturn(Optional.of(pharmacy));
        when(drugRepository.findById(3L)).thenReturn(Optional.of(drug));
        when(pharmacy.isReservationSupported()).thenReturn(false);
        when(pharmacy.getName()).thenReturn("No-Reservation Pharmacy");

        assertThatThrownBy(() -> newService().createReservation(svnr, 5L, 3L, 2))
                .isInstanceOf(ReservationNotSupportedException.class)
                .hasMessageContaining("No-Reservation Pharmacy");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservationPropagatesNotFoundForUnknownSvnr() {
        String svnr = "0000000000";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().createReservation(svnr, 5L, 3L, 1))
                .isInstanceOf(PatientNotFoundException.class);
        verify(pharmacyRepository, never()).findById(any());
    }

    @Test
    void cancelReservationTreatsAnotherPatientsReservationAsNotFound() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(otherPatient.getId()).thenReturn(2L);
        Reservation reservation = new Reservation(otherPatient, pharmacy, drug, 1, ReservationStatus.REQUESTED,
                Instant.now());
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> newService().cancelReservation(svnr, 99L))
                .isInstanceOf(NoSuchElementException.class);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancelReservationSetsStatusToCancelledForTheOwningPatient() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        Reservation reservation = new Reservation(patient, pharmacy, drug, 1, ReservationStatus.REQUESTED,
                Instant.now());
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));

        newService().cancelReservation(svnr, 99L);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verify(reservationRepository).save(reservation);
    }
}
