CREATE TABLE pharmacy (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY,
    name                  VARCHAR(255)     NOT NULL,
    address               VARCHAR(255)     NOT NULL,
    latitude              DOUBLE PRECISION NOT NULL,
    longitude             DOUBLE PRECISION NOT NULL,
    wheelchair_accessible BOOLEAN          NOT NULL,
    phone                 VARCHAR(50)      NOT NULL,
    email                 VARCHAR(255)     NOT NULL,
    website               VARCHAR(255),
    reservation_supported BOOLEAN          NOT NULL,
    CONSTRAINT pk_pharmacy PRIMARY KEY (id)
);

CREATE TABLE pharmacy_opening_hours (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    pharmacy_id BIGINT      NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    opens_at    TIME        NOT NULL,
    closes_at   TIME        NOT NULL,
    kind        VARCHAR(20) NOT NULL,
    CONSTRAINT pk_pharmacy_opening_hours PRIMARY KEY (id),
    CONSTRAINT fk_pharmacy_opening_hours_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacy (id)
);

CREATE INDEX idx_pharmacy_opening_hours_pharmacy ON pharmacy_opening_hours (pharmacy_id);

CREATE TABLE pharmacy_inventory (
    id           BIGINT GENERATED ALWAYS AS IDENTITY,
    pharmacy_id  BIGINT         NOT NULL,
    drug_id      BIGINT         NOT NULL,
    in_stock     BOOLEAN        NOT NULL,
    price        NUMERIC(10, 2) NOT NULL,
    last_updated TIMESTAMPTZ    NOT NULL,
    CONSTRAINT pk_pharmacy_inventory PRIMARY KEY (id),
    CONSTRAINT fk_pharmacy_inventory_pharmacy FOREIGN KEY (pharmacy_id) REFERENCES pharmacy (id),
    CONSTRAINT fk_pharmacy_inventory_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
);

CREATE INDEX idx_pharmacy_inventory_pharmacy ON pharmacy_inventory (pharmacy_id);
CREATE INDEX idx_pharmacy_inventory_drug ON pharmacy_inventory (drug_id);
