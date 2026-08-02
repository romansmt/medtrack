package com.medtrack.api.controller;

import com.medtrack.application.dto.CreateScheduleRequest;
import com.medtrack.application.dto.DueMedicationResponse;
import com.medtrack.application.dto.MedicationScheduleResponse;
import com.medtrack.application.dto.RecordIntakeRequest;
import com.medtrack.application.service.MedicationScheduleService;
import com.medtrack.domain.IntakeStatus;
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
@RequestMapping("/api/patients/{svnr}/medication-schedule")
@Tag(name = "Medication schedule", description = "A patient's tracked medications, due-today reminders, and intake confirmations (Einnahmeplan)")
public class MedicationScheduleController {

    private final MedicationScheduleService medicationScheduleService;

    public MedicationScheduleController(MedicationScheduleService medicationScheduleService) {
        this.medicationScheduleService = medicationScheduleService;
    }

    @GetMapping
    @Operation(summary = "List a patient's medication schedules")
    public List<MedicationScheduleResponse> listSchedules(@PathVariable String svnr) {
        return medicationScheduleService.listSchedules(svnr);
    }

    @PostMapping
    @Operation(summary = "Add a medication to a patient's schedule")
    public MedicationScheduleResponse createSchedule(@PathVariable String svnr,
                                                      @RequestBody CreateScheduleRequest request) {
        return medicationScheduleService.createSchedule(svnr, request.drugId(), request.doseText(),
                request.startDate(), request.endDate(), request.times());
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "Stop tracking a medication (deactivates the schedule, keeps its intake history)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateSchedule(@PathVariable String svnr, @PathVariable Long scheduleId) {
        medicationScheduleService.deactivateSchedule(svnr, scheduleId);
    }

    @GetMapping("/due-today")
    @Operation(summary = "Get today's due/confirmed/skipped doses across all active schedules")
    public List<DueMedicationResponse> getDueToday(@PathVariable String svnr) {
        return medicationScheduleService.getDueToday(svnr);
    }

    @PostMapping("/{scheduleTimeId}/intake")
    @Operation(summary = "Confirm or skip today's dose for a schedule time (Einnahme bestaetigen)")
    public DueMedicationResponse recordIntake(@PathVariable String svnr, @PathVariable Long scheduleTimeId,
                                               @RequestBody RecordIntakeRequest request) {
        return medicationScheduleService.recordIntake(svnr, scheduleTimeId, IntakeStatus.valueOf(request.status()));
    }
}
