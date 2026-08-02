-- All pharmacy names, addresses, and contact details below are original fictional inventions for
-- this study project - not real Vienna pharmacies.
INSERT INTO pharmacy (name, address, latitude, longitude, wheelchair_accessible, phone, email, website, reservation_supported) VALUES
    ('Apotheke Zum Goldenen Loewen', 'Naglergasse 5, 1010 Wien', 48.2082, 16.3719, TRUE,
        '+43 1 5551201', 'info@goldenerloewen-apotheke.example', 'https://goldenerloewen-apotheke.example', TRUE),
    ('Stadtapotheke Wien-Mitte', 'Landstrasser Hauptstrasse 22, 1030 Wien', 48.1980, 16.3919, TRUE,
        '+43 1 5551202', 'info@wienmitte-apotheke.example', 'https://wienmitte-apotheke.example', TRUE),
    ('VitaNova-Apotheke', 'Neubaugasse 45, 1070 Wien', 48.2013, 16.3489, FALSE,
        '+43 1 5551203', 'info@vitanova-apotheke.example', NULL, TRUE),
    ('Apotheke Zur Blauen Schwalbe', 'Margaretenstrasse 60, 1050 Wien', 48.1908, 16.3592, TRUE,
        '+43 1 5551204', 'info@blaueschwalbe-apotheke.example', 'https://blaueschwalbe-apotheke.example', FALSE),
    ('Donaukanal-Apotheke', 'Obere Donaustrasse 15, 1020 Wien', 48.2183, 16.3825, TRUE,
        '+43 1 5551205', 'info@donaukanal-apotheke.example', 'https://donaukanal-apotheke.example', TRUE),
    ('Apotheke Am Alsergrund', 'Waehringer Strasse 30, 1090 Wien', 48.2249, 16.3558, FALSE,
        '+43 1 5551206', 'info@alsergrund-apotheke.example', NULL, FALSE),
    ('Rathaus-Apotheke', 'Lange Gasse 10, 1080 Wien', 48.2108, 16.3477, TRUE,
        '+43 1 5551207', 'info@rathaus-apotheke.example', 'https://rathaus-apotheke.example', TRUE),
    ('Apotheke Zur Alten Muehle', 'Mariahilfer Strasse 150, 1150 Wien', 48.1934, 16.3272, FALSE,
        '+43 1 5551208', 'info@altemuehle-apotheke.example', 'https://altemuehle-apotheke.example', FALSE);

-- Regular weekly hours: Mon-Fri 08:00-18:00, Sat 08:00-12:00, closed Sunday (no row) - same pattern
-- for all 8 pharmacies, matching typical Austrian pharmacy hours.
INSERT INTO pharmacy_opening_hours (pharmacy_id, day_of_week, opens_at, closes_at, kind)
SELECT p.id, d.day, TIME '08:00', TIME '18:00', 'REGULAR'
FROM pharmacy p
CROSS JOIN (VALUES ('MONDAY'), ('TUESDAY'), ('WEDNESDAY'), ('THURSDAY'), ('FRIDAY')) AS d(day);

INSERT INTO pharmacy_opening_hours (pharmacy_id, day_of_week, opens_at, closes_at, kind)
SELECT p.id, 'SATURDAY', TIME '08:00', TIME '12:00', 'REGULAR'
FROM pharmacy p;

-- On-call (Bereitschaftsdienst) rotation: one pharmacy covers each overnight/Sunday gap so that
-- "which pharmacy is on duty right now" always has an answer regardless of when the demo is run.
-- Apotheke Zur Alten Muehle deliberately does not participate in the rotation.
INSERT INTO pharmacy_opening_hours (pharmacy_id, day_of_week, opens_at, closes_at, kind) VALUES
    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Zum Goldenen Loewen'), 'MONDAY', TIME '18:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Zum Goldenen Loewen'), 'TUESDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'Stadtapotheke Wien-Mitte'), 'TUESDAY', TIME '18:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Stadtapotheke Wien-Mitte'), 'WEDNESDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'VitaNova-Apotheke'), 'WEDNESDAY', TIME '18:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'VitaNova-Apotheke'), 'THURSDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Zur Blauen Schwalbe'), 'THURSDAY', TIME '18:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Zur Blauen Schwalbe'), 'FRIDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'Donaukanal-Apotheke'), 'FRIDAY', TIME '18:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Donaukanal-Apotheke'), 'SATURDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Am Alsergrund'), 'SATURDAY', TIME '12:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Apotheke Am Alsergrund'), 'SUNDAY', TIME '00:00', TIME '08:00', 'ON_CALL'),

    ((SELECT id FROM pharmacy WHERE name = 'Rathaus-Apotheke'), 'SUNDAY', TIME '08:00', TIME '23:59', 'ON_CALL'),
    ((SELECT id FROM pharmacy WHERE name = 'Rathaus-Apotheke'), 'MONDAY', TIME '00:00', TIME '08:00', 'ON_CALL');

-- Inventory: 4 "full service" pharmacies stock all 12 catalog rows, 4 smaller ones stock a subset.
-- Prices are fictional/illustrative and vary slightly by pharmacy. A handful of rows are marked
-- out of stock (in_stock = FALSE) rather than omitted, distinct from pharmacies that don't carry a
-- drug at all (no row) - both states are meaningful in the availability-check feature.
INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Apotheke Zum Goldenen Loewen'),
    d.id,
    TRUE,
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.50), ('10000002', 5.20), ('10000003', 3.80), ('10000004', 12.90),
    ('10000005', 8.40), ('10000006', 9.60), ('10000007', 2.50), ('10000008', 15.00),
    ('10000009', 6.90), ('10000010', 7.20), ('10000011', 6.50), ('10000012', 11.30)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Stadtapotheke Wien-Mitte'),
    d.id,
    (base.pzn <> '10000004'),
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.70), ('10000002', 5.00), ('10000003', 3.95), ('10000004', 13.20),
    ('10000005', 8.10), ('10000006', 9.90), ('10000007', 2.60), ('10000008', 14.50),
    ('10000009', 7.10), ('10000010', 7.00), ('10000011', 6.80), ('10000012', 11.60)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'VitaNova-Apotheke'),
    d.id,
    TRUE,
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.30), ('10000002', 5.35), ('10000003', 3.70), ('10000004', 12.60),
    ('10000005', 8.60), ('10000006', 9.40), ('10000007', 2.45), ('10000008', 15.40),
    ('10000009', 6.75), ('10000010', 7.40), ('10000011', 6.30), ('10000012', 11.10)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Donaukanal-Apotheke'),
    d.id,
    (base.pzn <> '10000010'),
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.60), ('10000002', 5.15), ('10000003', 3.85), ('10000004', 13.00),
    ('10000005', 8.30), ('10000006', 9.70), ('10000007', 2.55), ('10000008', 14.80),
    ('10000009', 6.95), ('10000010', 7.10), ('10000011', 6.60), ('10000012', 11.40)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

-- Smaller pharmacies: everyday OTC + the two most common Rx drugs only.
INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Apotheke Zur Blauen Schwalbe'),
    d.id,
    TRUE,
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.55), ('10000002', 5.10), ('10000003', 3.90), ('10000007', 2.65),
    ('10000004', 13.10), ('10000005', 8.50), ('10000011', 6.70)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Apotheke Am Alsergrund'),
    d.id,
    (base.pzn <> '10000002'),
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.40), ('10000002', 5.25), ('10000003', 3.75), ('10000007', 2.40),
    ('10000006', 9.80), ('10000012', 11.50)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Rathaus-Apotheke'),
    d.id,
    TRUE,
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.65), ('10000002', 5.05), ('10000003', 3.80), ('10000007', 2.50),
    ('10000005', 8.20), ('10000010', 7.30), ('10000011', 6.40)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;

INSERT INTO pharmacy_inventory (pharmacy_id, drug_id, in_stock, price, last_updated)
SELECT
    (SELECT id FROM pharmacy WHERE name = 'Apotheke Zur Alten Muehle'),
    d.id,
    (base.pzn <> '10000003'),
    base.base_price,
    CURRENT_TIMESTAMP
FROM (VALUES
    ('10000001', 4.75), ('10000002', 4.95), ('10000003', 4.00), ('10000004', 12.70),
    ('10000009', 6.85)
) AS base(pzn, base_price)
JOIN drug d ON d.pzn = base.pzn;
