package com.medtrack.application.service;

import com.medtrack.application.dto.DueMedicationResponse;
import com.medtrack.application.dto.MedicationScheduleResponse;
import com.medtrack.application.dto.ScheduleTimeResponse;
import com.medtrack.domain.Drug;
import com.medtrack.domain.IntakeStatus;
import com.medtrack.domain.MedicationIntakeLog;
import com.medtrack.domain.MedicationSchedule;
import com.medtrack.domain.MedicationScheduleTime;
import com.medtrack.domain.Patient;
import com.medtrack.domain.PatientNotFoundException;
import com.medtrack.infrastructure.persistence.DrugRepository;
import com.medtrack.infrastructure.persistence.MedicationIntakeLogRepository;
import com.medtrack.infrastructure.persistence.MedicationScheduleRepository;
import com.medtrack.infrastructure.persistence.MedicationScheduleTimeRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class MedicationScheduleService {

    private final PatientRepository patientRepository;
    private final DrugRepository drugRepository;
    private final MedicationScheduleRepository scheduleRepository;
    private final MedicationScheduleTimeRepository scheduleTimeRepository;
    private final MedicationIntakeLogRepository intakeLogRepository;

    public MedicationScheduleService(PatientRepository patientRepository, DrugRepository drugRepository,
                                      MedicationScheduleRepository scheduleRepository,
                                      MedicationScheduleTimeRepository scheduleTimeRepository,
                                      MedicationIntakeLogRepository intakeLogRepository) {
        this.patientRepository = patientRepository;
        this.drugRepository = drugRepository;
        this.scheduleRepository = scheduleRepository;
        this.scheduleTimeRepository = scheduleTimeRepository;
        this.intakeLogRepository = intakeLogRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicationScheduleResponse> listSchedules(String svnr) {
        Patient patient = findPatientOrThrow(svnr);
        List<MedicationSchedule> schedules = scheduleRepository.findByPatientId(patient.getId());
        Map<Long, List<MedicationScheduleTime>> timesBySchedule = groupTimesBySchedule(schedules);

        return schedules.stream()
                .map(schedule -> toScheduleResponse(schedule, timesBySchedule.getOrDefault(schedule.getId(), List.of())))
                .toList();
    }

    @Transactional
    public MedicationScheduleResponse createSchedule(String svnr, Long drugId, String doseText, LocalDate startDate,
                                                      LocalDate endDate, List<LocalTime> times) {
        Patient patient = findPatientOrThrow(svnr);
        Drug drug = drugRepository.findById(drugId)
                .orElseThrow(() -> new NoSuchElementException("No drug for id " + drugId));

        MedicationSchedule schedule = scheduleRepository.save(
                new MedicationSchedule(patient, drug, doseText, startDate, endDate, true));

        List<MedicationScheduleTime> scheduleTimes = scheduleTimeRepository.saveAll(times.stream()
                .map(time -> new MedicationScheduleTime(schedule, time))
                .toList());

        return toScheduleResponse(schedule, scheduleTimes);
    }

    @Transactional
    public void deactivateSchedule(String svnr, Long scheduleId) {
        Patient patient = findPatientOrThrow(svnr);
        MedicationSchedule schedule = findScheduleOwnedByPatient(scheduleId, patient);

        schedule.setActive(false);
        scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public List<DueMedicationResponse> getDueToday(String svnr) {
        Patient patient = findPatientOrThrow(svnr);
        LocalDate today = LocalDate.now();

        List<Long> activeScheduleIds = scheduleRepository.findByPatientIdAndActiveTrue(patient.getId()).stream()
                .filter(schedule -> isWithinRange(schedule, today))
                .map(MedicationSchedule::getId)
                .toList();

        List<MedicationScheduleTime> times = scheduleTimeRepository.findByScheduleIdIn(activeScheduleIds);
        List<Long> timeIds = times.stream().map(MedicationScheduleTime::getId).toList();

        Map<Long, MedicationIntakeLog> logsByTimeId = intakeLogRepository
                .findByScheduleTimeIdInAndScheduledDate(timeIds, today).stream()
                .collect(Collectors.toMap(log -> log.getScheduleTime().getId(), log -> log));

        return times.stream()
                .map(time -> toDueResponse(time, today, logsByTimeId.get(time.getId())))
                .sorted(Comparator.comparing(DueMedicationResponse::timeOfDay))
                .toList();
    }

    @Transactional
    public DueMedicationResponse recordIntake(String svnr, Long scheduleTimeId, IntakeStatus status) {
        Patient patient = findPatientOrThrow(svnr);
        MedicationScheduleTime scheduleTime = scheduleTimeRepository.findById(scheduleTimeId)
                .orElseThrow(() -> new NoSuchElementException("No schedule time for id " + scheduleTimeId));

        if (!scheduleTime.getSchedule().getPatient().getId().equals(patient.getId())) {
            throw new NoSuchElementException("No schedule time for id " + scheduleTimeId);
        }

        LocalDate today = LocalDate.now();
        Instant now = Instant.now();

        MedicationIntakeLog log = intakeLogRepository.findByScheduleTimeIdAndScheduledDate(scheduleTimeId, today)
                .orElseGet(() -> new MedicationIntakeLog(scheduleTime, today, status, now));
        log.setStatus(status);
        log.setConfirmedAt(now);

        intakeLogRepository.save(log);
        return toDueResponse(scheduleTime, today, log);
    }

    private MedicationSchedule findScheduleOwnedByPatient(Long scheduleId, Patient patient) {
        MedicationSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NoSuchElementException("No schedule for id " + scheduleId));

        if (!schedule.getPatient().getId().equals(patient.getId())) {
            throw new NoSuchElementException("No schedule for id " + scheduleId);
        }
        return schedule;
    }

    private boolean isWithinRange(MedicationSchedule schedule, LocalDate today) {
        return !schedule.getStartDate().isAfter(today)
                && (schedule.getEndDate() == null || !schedule.getEndDate().isBefore(today));
    }

    private Map<Long, List<MedicationScheduleTime>> groupTimesBySchedule(List<MedicationSchedule> schedules) {
        List<Long> scheduleIds = schedules.stream().map(MedicationSchedule::getId).toList();
        return scheduleTimeRepository.findByScheduleIdIn(scheduleIds).stream()
                .collect(Collectors.groupingBy(time -> time.getSchedule().getId()));
    }

    private MedicationScheduleResponse toScheduleResponse(MedicationSchedule schedule,
                                                            List<MedicationScheduleTime> times) {
        List<ScheduleTimeResponse> timeResponses = times.stream()
                .sorted(Comparator.comparing(MedicationScheduleTime::getTimeOfDay))
                .map(time -> new ScheduleTimeResponse(time.getId(), time.getTimeOfDay()))
                .toList();

        return new MedicationScheduleResponse(schedule.getId(), schedule.getDrug().getId(),
                schedule.getDrug().getName(), schedule.getDoseText(), schedule.getStartDate(),
                schedule.getEndDate(), schedule.isActive(), timeResponses);
    }

    private DueMedicationResponse toDueResponse(MedicationScheduleTime time, LocalDate today,
                                                 MedicationIntakeLog log) {
        MedicationSchedule schedule = time.getSchedule();
        String status = log != null ? log.getStatus().toString() : "PENDING";
        Instant confirmedAt = log != null ? log.getConfirmedAt() : null;

        return new DueMedicationResponse(schedule.getId(), time.getId(), schedule.getDrug().getName(),
                schedule.getDoseText(), time.getTimeOfDay(), today, status, confirmedAt);
    }

    private Patient findPatientOrThrow(String svnr) {
        return patientRepository.findBySvnr(svnr)
                .orElseThrow(() -> new PatientNotFoundException(svnr));
    }
}
