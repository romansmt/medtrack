package com.medtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

// A row ID Austria (not MedTrack) owns - the source of truth that registration/login verification
// is checked against. See id_austria_registry (V11__id_austria_registry.sql) and
// IdAustriaRegistryRepository, which only the ID-Austria adapter package is allowed to touch.
@Entity
@Table(name = "id_austria_registry")
public class IdAustriaRecord {

    @Id
    @Column(name = "svnr", length = 10)
    private String svnr;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "card_serial_number", nullable = false, length = 20)
    private String cardSerialNumber;

    @Column(name = "carrier_number", nullable = false, length = 4)
    private String carrierNumber;

    @Column(name = "insurer_name", nullable = false)
    private String insurerName;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    protected IdAustriaRecord() {
    }

    public IdAustriaRecord(
            String svnr,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String cardSerialNumber,
            String carrierNumber,
            String insurerName,
            LocalDate expiryDate) {
        this.svnr = svnr;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.cardSerialNumber = cardSerialNumber;
        this.carrierNumber = carrierNumber;
        this.insurerName = insurerName;
        this.expiryDate = expiryDate;
    }

    public String getSvnr() {
        return svnr;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public String getCarrierNumber() {
        return carrierNumber;
    }

    public String getInsurerName() {
        return insurerName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }
}
