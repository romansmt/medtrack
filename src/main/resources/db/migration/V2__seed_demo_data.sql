INSERT INTO patient (svnr, name) VALUES
    ('1234010190', 'Anna Gruber'),
    ('2345020285', 'Max Bauer'),
    ('3456030380', 'Lena Hofer'),
    ('4567040475', 'Paul Wagner');

INSERT INTO doctor (name, specialty) VALUES
    ('Dr. Julia Steiner', 'General Practitioner'),
    ('Dr. Thomas Berger', 'Cardiology'),
    ('Dr. Marie Huber', 'Dermatology');

INSERT INTO drug (name, active_substance) VALUES
    ('Aspirin', 'Acetylsalicylic acid'),
    ('Nurofen', 'Ibuprofen'),
    ('Paracetamol', 'Paracetamol'),
    ('Amoxicillin', 'Amoxicillin'),
    ('Metformin', 'Metformin'),
    ('Omeprazole', 'Omeprazole');

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '1234010190'),
    (SELECT id FROM doctor WHERE name = 'Dr. Julia Steiner'),
    (SELECT id FROM drug WHERE name = 'Aspirin'),
    '1 tablet daily',
    DATE '2026-06-01',
    'OPEN'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '1234010190'),
    (SELECT id FROM doctor WHERE name = 'Dr. Marie Huber'),
    (SELECT id FROM drug WHERE name = 'Paracetamol'),
    '500mg every 6 hours as needed',
    DATE '2026-06-10',
    'REDEEMED'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '2345020285'),
    (SELECT id FROM doctor WHERE name = 'Dr. Thomas Berger'),
    (SELECT id FROM drug WHERE name = 'Metformin'),
    '850mg twice daily with meals',
    DATE '2026-05-20',
    'REDEEMED'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '2345020285'),
    (SELECT id FROM doctor WHERE name = 'Dr. Julia Steiner'),
    (SELECT id FROM drug WHERE name = 'Omeprazole'),
    '20mg once daily before breakfast',
    DATE '2026-07-01',
    'OPEN'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '3456030380'),
    (SELECT id FROM doctor WHERE name = 'Dr. Marie Huber'),
    (SELECT id FROM drug WHERE name = 'Nurofen'),
    '400mg up to three times daily',
    DATE '2026-06-25',
    'OPEN'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '3456030380'),
    (SELECT id FROM doctor WHERE name = 'Dr. Thomas Berger'),
    (SELECT id FROM drug WHERE name = 'Aspirin'),
    '100mg once daily',
    DATE '2026-07-05',
    'OPEN'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '4567040475'),
    (SELECT id FROM doctor WHERE name = 'Dr. Julia Steiner'),
    (SELECT id FROM drug WHERE name = 'Amoxicillin'),
    '500mg three times daily for 7 days',
    DATE '2026-06-15',
    'REDEEMED'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '4567040475'),
    (SELECT id FROM doctor WHERE name = 'Dr. Marie Huber'),
    (SELECT id FROM drug WHERE name = 'Paracetamol'),
    '500mg every 6 hours as needed',
    DATE '2026-07-10',
    'OPEN'
);

INSERT INTO prescription (patient_id, doctor_id, drug_id, dosage, issued_date, status)
VALUES (
    (SELECT id FROM patient WHERE svnr = '4567040475'),
    (SELECT id FROM doctor WHERE name = 'Dr. Thomas Berger'),
    (SELECT id FROM drug WHERE name = 'Metformin'),
    '850mg twice daily with meals',
    DATE '2026-05-30',
    'REDEEMED'
);
