package com.medtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Austrian SVNR: 10-digit social insurance number, unique per patient.
    @Column(name = "svnr", nullable = false, unique = true, length = 10)
    private String svnr;

    @Column(name = "name", nullable = false)
    private String name;

    protected Patient() {
    }

    public Patient(String svnr, String name) {
        this.svnr = svnr;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getSvnr() {
        return svnr;
    }

    public void setSvnr(String svnr) {
        this.svnr = svnr;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
