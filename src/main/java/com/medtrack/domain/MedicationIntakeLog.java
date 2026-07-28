package com.medtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;

// Only ever created at the moment a patient confirms or skips a dose - a scheduled dose with no row
// yet for (scheduleTime, scheduledDate) is implicitly PENDING. Keeps this lean/computed-on-read,
// with no background job pre-generating rows for doses nobody has acted on yet.
@Entity
@Table(name = "medication_intake_log")
public class MedicationIntakeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_time_id", nullable = false)
    private MedicationScheduleTime scheduleTime;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IntakeStatus status;

    @Column(name = "confirmed_at", nullable = false)
    private Instant confirmedAt;

    protected MedicationIntakeLog() {
    }

    public MedicationIntakeLog(MedicationScheduleTime scheduleTime, LocalDate scheduledDate, IntakeStatus status,
                                Instant confirmedAt) {
        this.scheduleTime = scheduleTime;
        this.scheduledDate = scheduledDate;
        this.status = status;
        this.confirmedAt = confirmedAt;
    }

    public Long getId() {
        return id;
    }

    public MedicationScheduleTime getScheduleTime() {
        return scheduleTime;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public IntakeStatus getStatus() {
        return status;
    }

    public void setStatus(IntakeStatus status) {
        this.status = status;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(Instant confirmedAt) {
        this.confirmedAt = confirmedAt;
    }
}
