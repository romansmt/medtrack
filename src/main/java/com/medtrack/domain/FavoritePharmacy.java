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

import java.time.Instant;

@Entity
@Table(name = "favorite_pharmacy")
public class FavoritePharmacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FavoritePharmacy() {
    }

    public FavoritePharmacy(Patient patient, Pharmacy pharmacy, Instant createdAt) {
        this.patient = patient;
        this.pharmacy = pharmacy;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
