package com.medtrack.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "drug")
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "active_substance", nullable = false)
    private String activeSubstance;

    // e.g. "Tabletten", "Filmtabletten", "Infusionsloesung" - distinct catalog rows share a name/substance
    // but differ by form/packSize/manufacturer, matching how real availability search results are listed.
    @Column(name = "form", nullable = false)
    private String form;

    @Column(name = "pack_size", nullable = false)
    private String packSize;

    @Column(name = "manufacturer", nullable = false)
    private String manufacturer;

    @Column(name = "prescription_required", nullable = false)
    private boolean prescriptionRequired;

    // Fake Pharmazentralnummer - shaped like the real Austrian/German registry number, never a real one.
    @Column(name = "pzn", nullable = false, unique = true, length = 8)
    private String pzn;

    @Column(name = "package_leaflet_text", columnDefinition = "TEXT")
    private String packageLeafletText;

    protected Drug() {
    }

    public Drug(String name, String activeSubstance, String form, String packSize, String manufacturer,
                boolean prescriptionRequired, String pzn, String packageLeafletText) {
        this.name = name;
        this.activeSubstance = activeSubstance;
        this.form = form;
        this.packSize = packSize;
        this.manufacturer = manufacturer;
        this.prescriptionRequired = prescriptionRequired;
        this.pzn = pzn;
        this.packageLeafletText = packageLeafletText;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveSubstance() {
        return activeSubstance;
    }

    public void setActiveSubstance(String activeSubstance) {
        this.activeSubstance = activeSubstance;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getPackSize() {
        return packSize;
    }

    public void setPackSize(String packSize) {
        this.packSize = packSize;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public boolean isPrescriptionRequired() {
        return prescriptionRequired;
    }

    public void setPrescriptionRequired(boolean prescriptionRequired) {
        this.prescriptionRequired = prescriptionRequired;
    }

    public String getPzn() {
        return pzn;
    }

    public void setPzn(String pzn) {
        this.pzn = pzn;
    }

    public String getPackageLeafletText() {
        return packageLeafletText;
    }

    public void setPackageLeafletText(String packageLeafletText) {
        this.packageLeafletText = packageLeafletText;
    }
}
