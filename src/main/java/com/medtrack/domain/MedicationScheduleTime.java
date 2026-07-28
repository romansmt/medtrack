package com.medtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalTime;

@Entity
@Table(name = "medication_schedule_time")
public class MedicationScheduleTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MedicationSchedule schedule;

    @Column(name = "time_of_day", nullable = false)
    private LocalTime timeOfDay;

    protected MedicationScheduleTime() {
    }

    public MedicationScheduleTime(MedicationSchedule schedule, LocalTime timeOfDay) {
        this.schedule = schedule;
        this.timeOfDay = timeOfDay;
    }

    public Long getId() {
        return id;
    }

    public MedicationSchedule getSchedule() {
        return schedule;
    }

    public LocalTime getTimeOfDay() {
        return timeOfDay;
    }
}
