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

import java.time.LocalDate;

@Entity
@Table(name = "medication_schedule")
public class MedicationSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_id", nullable = false)
    private Drug drug;

    @Column(name = "dose_text", nullable = false)
    private String doseText;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Null means "ongoing, no planned end date".
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected MedicationSchedule() {
    }

    public MedicationSchedule(Patient patient, Drug drug, String doseText, LocalDate startDate, LocalDate endDate,
                               boolean active) {
        this.patient = patient;
        this.drug = drug;
        this.doseText = doseText;
        this.startDate = startDate;
        this.endDate = endDate;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Drug getDrug() {
        return drug;
    }

    public String getDoseText() {
        return doseText;
    }

    public void setDoseText(String doseText) {
        this.doseText = doseText;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
