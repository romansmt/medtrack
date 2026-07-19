CREATE TABLE patient (
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    svnr VARCHAR(10)  NOT NULL,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_patient PRIMARY KEY (id),
    CONSTRAINT uq_patient_svnr UNIQUE (svnr)
);

CREATE TABLE doctor (
    id        BIGINT GENERATED ALWAYS AS IDENTITY,
    name      VARCHAR(255) NOT NULL,
    specialty VARCHAR(255) NOT NULL,
    CONSTRAINT pk_doctor PRIMARY KEY (id)
);

CREATE TABLE drug (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    name             VARCHAR(255) NOT NULL,
    active_substance VARCHAR(255) NOT NULL,
    CONSTRAINT pk_drug PRIMARY KEY (id)
);

CREATE TABLE prescription (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    patient_id  BIGINT       NOT NULL,
    doctor_id   BIGINT       NOT NULL,
    drug_id     BIGINT       NOT NULL,
    dosage      VARCHAR(255) NOT NULL,
    issued_date DATE         NOT NULL,
    status      VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_prescription PRIMARY KEY (id),
    CONSTRAINT fk_prescription_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_prescription_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    CONSTRAINT fk_prescription_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
);
