CREATE TABLE medication_schedule (
    id         BIGINT GENERATED ALWAYS AS IDENTITY,
    patient_id BIGINT       NOT NULL,
    drug_id    BIGINT       NOT NULL,
    dose_text  VARCHAR(255) NOT NULL,
    start_date DATE         NOT NULL,
    end_date   DATE,
    active     BOOLEAN      NOT NULL,
    CONSTRAINT pk_medication_schedule PRIMARY KEY (id),
    CONSTRAINT fk_medication_schedule_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_medication_schedule_drug FOREIGN KEY (drug_id) REFERENCES drug (id)
);

CREATE INDEX idx_medication_schedule_patient ON medication_schedule (patient_id);

CREATE TABLE medication_schedule_time (
    id          BIGINT GENERATED ALWAYS AS IDENTITY,
    schedule_id BIGINT NOT NULL,
    time_of_day TIME   NOT NULL,
    CONSTRAINT pk_medication_schedule_time PRIMARY KEY (id),
    CONSTRAINT fk_medication_schedule_time_schedule FOREIGN KEY (schedule_id) REFERENCES medication_schedule (id)
);

CREATE INDEX idx_medication_schedule_time_schedule ON medication_schedule_time (schedule_id);

-- Only ever gets a row when a patient actually confirms/skips a dose - a scheduled dose with no
-- row yet for (schedule_time_id, scheduled_date) is implicitly still pending. No background job
-- pre-generates rows for doses nobody has acted on.
CREATE TABLE medication_intake_log (
    id               BIGINT GENERATED ALWAYS AS IDENTITY,
    schedule_time_id BIGINT      NOT NULL,
    scheduled_date   DATE        NOT NULL,
    status           VARCHAR(20) NOT NULL,
    confirmed_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_medication_intake_log PRIMARY KEY (id),
    CONSTRAINT fk_medication_intake_log_schedule_time FOREIGN KEY (schedule_time_id)
        REFERENCES medication_schedule_time (id),
    CONSTRAINT uq_medication_intake_log_schedule_time_date UNIQUE (schedule_time_id, scheduled_date)
);
