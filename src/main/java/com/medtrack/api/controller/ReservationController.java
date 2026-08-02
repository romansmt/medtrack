package com.medtrack.api.controller;

import com.medtrack.application.dto.CreateReservationRequest;
import com.medtrack.application.dto.ReservationResponse;
import com.medtrack.application.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{svnr}/reservations")
@Tag(name = "Reservations", description = "A patient's medication reservations at pharmacies")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "List a patient's reservations")
    public List<ReservationResponse> listReservations(@PathVariable String svnr) {
        return reservationService.listReservations(svnr);
    }

    @PostMapping
    @Operation(summary = "Create a reservation for a medication at a pharmacy")
    public ReservationResponse createReservation(@PathVariable String svnr,
                                                  @RequestBody CreateReservationRequest request) {
        return reservationService.createReservation(svnr, request.pharmacyId(), request.drugId(),
                request.quantity());
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelReservation(@PathVariable String svnr, @PathVariable Long reservationId) {
        reservationService.cancelReservation(svnr, reservationId);
    }
}
