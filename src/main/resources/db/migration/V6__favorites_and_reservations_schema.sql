CREATE TABLE favorite_pharmacy (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    patient_id  BIGINT      NOT NULL,
    pharmacy_id BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_favorite_pharmacy PRIMARY KEY (id),
    CONSTRAINT fk_favorite_pharmacy_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_favorite_pharmacy_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacy (id),
    CONSTRAINT uq_favorite_pharmacy_patient_pharmacy UNIQUE (patient_id, pharmacy_id)
);

CREATE TABLE favorite_drug (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    patient_id BIGINT      NOT NULL,
    drug_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_favorite_drug PRIMARY KEY (id),
    CONSTRAINT fk_favorite_drug_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_favorite_drug_drug FOREIGN KEY (drug_id) REFERENCES drug (id),
    CONSTRAINT uq_favorite_drug_patient_drug UNIQUE (patient_id, drug_id)
);

CREATE TABLE reservation (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    patient_id   BIGINT      NOT NULL,
    pharmacy_id  BIGINT      NOT NULL,
    drug_id      BIGINT      NOT NULL,
    quantity     INTEGER     NOT NULL,
    status       VARCHAR(20) NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_reservation PRIMARY KEY (id),
    CONSTRAINT fk_reservation_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_reservation_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacy (id),
    CONSTRAINT fk_reservation_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
);

CREATE INDEX idx_favorite_pharmacy_patient ON favorite_pharmacy (patient_id);
CREATE INDEX idx_favorite_drug_patient ON favorite_drug (patient_id);
CREATE INDEX idx_reservation_patient ON reservation (patient_id);
