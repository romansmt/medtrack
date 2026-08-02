package com.medtrack.application.service;

import com.medtrack.application.dto.DueMedicationResponse;
import com.medtrack.domain.Drug;
import com.medtrack.domain.IntakeStatus;
import com.medtrack.domain.MedicationIntakeLog;
import com.medtrack.domain.MedicationSchedule;
import com.medtrack.domain.MedicationScheduleTime;
import com.medtrack.domain.Patient;
import com.medtrack.infrastructure.persistence.DrugRepository;
import com.medtrack.infrastructure.persistence.MedicationIntakeLogRepository;
import com.medtrack.infrastructure.persistence.MedicationScheduleRepository;
import com.medtrack.infrastructure.persistence.MedicationScheduleTimeRepository;
import com.medtrack.infrastructure.persistence.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MedicationScheduleServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DrugRepository drugRepository;
    @Mock
    private MedicationScheduleRepository scheduleRepository;
    @Mock
    private MedicationScheduleTimeRepository scheduleTimeRepository;
    @Mock
    private MedicationIntakeLogRepository intakeLogRepository;

    @Mock
    private Patient patient;
    @Mock
    private Patient otherPatient;
    @Mock
    private Drug drug;
    @Mock
    private MedicationSchedule schedule;
    @Mock
    private MedicationScheduleTime scheduleTime;

    private MedicationScheduleService newService() {
        return new MedicationScheduleService(patientRepository, drugRepository, scheduleRepository,
                scheduleTimeRepository, intakeLogRepository);
    }

    @Test
    void recordIntakeCreatesANewLogWhenNoneExistsForToday() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(scheduleTimeRepository.findById(10L)).thenReturn(Optional.of(scheduleTime));
        when(scheduleTime.getSchedule()).thenReturn(schedule);
        when(schedule.getPatient()).thenReturn(patient);
        when(schedule.getDrug()).thenReturn(drug);
        when(schedule.getDoseText()).thenReturn("1 Tablette");
        when(drug.getName()).thenReturn("Aspirin");
        when(intakeLogRepository.findByScheduleTimeIdAndScheduledDate(10L, LocalDate.now()))
                .thenReturn(Optional.empty());
        when(intakeLogRepository.save(any(MedicationIntakeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        DueMedicationResponse response = newService().recordIntake(svnr, 10L, IntakeStatus.TAKEN);

        assertThat(response.status()).isEqualTo("TAKEN");
        assertThat(response.drugName()).isEqualTo("Aspirin");
        verify(intakeLogRepository).save(any(MedicationIntakeLog.class));
    }

    @Test
    void recordIntakeUpdatesAnExistingLogInsteadOfCreatingASecondOne() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(scheduleTimeRepository.findById(10L)).thenReturn(Optional.of(scheduleTime));
        when(scheduleTime.getSchedule()).thenReturn(schedule);
        when(schedule.getPatient()).thenReturn(patient);
        when(schedule.getDrug()).thenReturn(drug);

        MedicationIntakeLog existing = new MedicationIntakeLog(scheduleTime, LocalDate.now(), IntakeStatus.SKIPPED,
                Instant.now().minusSeconds(3600));
        when(intakeLogRepository.findByScheduleTimeIdAndScheduledDate(10L, LocalDate.now()))
                .thenReturn(Optional.of(existing));
        when(intakeLogRepository.save(any(MedicationIntakeLog.class))).thenAnswer(inv -> inv.getArgument(0));

        DueMedicationResponse response = newService().recordIntake(svnr, 10L, IntakeStatus.TAKEN);

        assertThat(response.status()).isEqualTo("TAKEN");
        assertThat(existing.getStatus()).isEqualTo(IntakeStatus.TAKEN);
        verify(intakeLogRepository).save(existing);
    }

    @Test
    void recordIntakeRejectsAScheduleTimeBelongingToAnotherPatient() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(otherPatient.getId()).thenReturn(2L);
        when(scheduleTimeRepository.findById(10L)).thenReturn(Optional.of(scheduleTime));
        when(scheduleTime.getSchedule()).thenReturn(schedule);
        when(schedule.getPatient()).thenReturn(otherPatient);

        assertThatThrownBy(() -> newService().recordIntake(svnr, 10L, IntakeStatus.TAKEN))
                .isInstanceOf(NoSuchElementException.class);
        verify(intakeLogRepository, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void dueTodayReportsPendingWhenNoIntakeLogExistsYet() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(schedule.getId()).thenReturn(100L);
        when(schedule.getStartDate()).thenReturn(LocalDate.now().minusDays(5));
        when(schedule.getEndDate()).thenReturn(null);
        when(schedule.getDrug()).thenReturn(drug);
        when(schedule.getDoseText()).thenReturn("1 Tablette");
        when(drug.getName()).thenReturn("Aspirin");
        when(scheduleRepository.findByPatientIdAndActiveTrue(1L)).thenReturn(List.of(schedule));

        when(scheduleTime.getId()).thenReturn(10L);
        when(scheduleTime.getSchedule()).thenReturn(schedule);
        when(scheduleTime.getTimeOfDay()).thenReturn(LocalTime.of(8, 0));
        when(scheduleTimeRepository.findByScheduleIdIn(List.of(100L))).thenReturn(List.of(scheduleTime));
        when(intakeLogRepository.findByScheduleTimeIdInAndScheduledDate(List.of(10L), LocalDate.now()))
                .thenReturn(List.of());

        List<DueMedicationResponse> result = newService().getDueToday(svnr);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo("PENDING");
        assertThat(result.get(0).confirmedAt()).isNull();
    }

    @Test
    void dueTodayExcludesASchedulePastItsEndDate() {
        String svnr = "1234010190";
        when(patientRepository.findBySvnr(svnr)).thenReturn(Optional.of(patient));
        when(patient.getId()).thenReturn(1L);
        when(schedule.getStartDate()).thenReturn(LocalDate.now().minusDays(30));
        when(schedule.getEndDate()).thenReturn(LocalDate.now().minusDays(1));
        when(scheduleRepository.findByPatientIdAndActiveTrue(1L)).thenReturn(List.of(schedule));
        when(scheduleTimeRepository.findByScheduleIdIn(List.of())).thenReturn(List.of());
        when(intakeLogRepository.findByScheduleTimeIdInAndScheduledDate(List.of(), LocalDate.now()))
                .thenReturn(List.of());

        List<DueMedicationResponse> result = newService().getDueToday(svnr);

        assertThat(result).isEmpty();
    }
}
