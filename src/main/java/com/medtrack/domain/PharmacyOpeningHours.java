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

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "pharmacy_opening_hours")
public class PharmacyOpeningHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacy_id", nullable = false)
    private Pharmacy pharmacy;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(name = "opens_at", nullable = false)
    private LocalTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private LocalTime closesAt;

    // REGULAR = normal weekly opening hours; ON_CALL = Bereitschaftsdienst (emergency duty) hours.
    // A shift spanning midnight (e.g. Mon 18:00-23:59 continuing Tue 00:00-08:00) is stored as two
    // same-day rows rather than one cross-midnight row, matching how the reference app displays it.
    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 20)
    private OpeningHoursKind kind;

    protected PharmacyOpeningHours() {
    }

    public PharmacyOpeningHours(Pharmacy pharmacy, DayOfWeek dayOfWeek, LocalTime opensAt,
                                 LocalTime closesAt, OpeningHoursKind kind) {
        this.pharmacy = pharmacy;
        this.dayOfWeek = dayOfWeek;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.kind = kind;
    }

    public Long getId() {
        return id;
    }

    public Pharmacy getPharmacy() {
        return pharmacy;
    }

    public void setPharmacy(Pharmacy pharmacy) {
        this.pharmacy = pharmacy;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(LocalTime opensAt) {
        this.opensAt = opensAt;
    }

    public LocalTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(LocalTime closesAt) {
        this.closesAt = closesAt;
    }

    public OpeningHoursKind getKind() {
        return kind;
    }

    public void setKind(OpeningHoursKind kind) {
        this.kind = kind;
    }
}
