-- Simulates the identity data that ID Austria (not MedTrack) owns and is the sole source of truth
-- for. MedTrack never writes to this table directly - only the mock ID-Austria adapter reads it, to
-- verify what a user submits during registration/login against "the real record". Values mirror
-- docs/TEST_CREDENTIALS.md exactly, which in turn mirror what MockEHealthCardAdapter used to compute
-- on the fly for the same four demo patients.
CREATE TABLE id_austria_registry (
    svnr               VARCHAR(10)  NOT NULL,
    first_name         VARCHAR(255) NOT NULL,
    last_name          VARCHAR(255) NOT NULL,
    date_of_birth      DATE         NOT NULL,
    card_serial_number VARCHAR(20)  NOT NULL,
    carrier_number     VARCHAR(4)   NOT NULL,
    insurer_name       VARCHAR(255) NOT NULL,
    expiry_date        DATE         NOT NULL,
    CONSTRAINT pk_id_austria_registry PRIMARY KEY (svnr)
);

INSERT INTO id_austria_registry
    (svnr, first_name, last_name, date_of_birth, card_serial_number, carrier_number, insurer_name, expiry_date)
VALUES
    ('1234010190', 'Anna', 'Gruber', DATE '1990-01-01', '12340101900910104321', '4711', 'ÖGK', DATE '2031-12-31'),
    ('2345020285', 'Max',  'Bauer',  DATE '1985-02-02', '23450202855820205432', '4711', 'ÖGK', DATE '2031-12-31'),
    ('3456030380', 'Lena', 'Hofer',  DATE '1980-03-03', '34560303800830306543', '4711', 'ÖGK', DATE '2031-12-31'),
    ('4567040475', 'Paul', 'Wagner', DATE '1975-04-04', '45670404755740407654', '4711', 'ÖGK', DATE '2031-12-31');
