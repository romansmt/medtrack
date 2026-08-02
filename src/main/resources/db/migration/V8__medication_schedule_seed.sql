-- Demo medication schedules for 3 of the 4 seeded patients, illustrating the adherence-tracking
-- feature (ApoApp's "Einnahmeplan"). Paul Wagner deliberately gets none, so the frontend also has
-- a real "no medications tracked yet" empty state to show.
INSERT INTO medication_schedule (patient_id, drug_id, dose_text, start_date, end_date, active) VALUES
    ((SELECT id FROM patient WHERE svnr = '1234010190'), (SELECT id FROM drug WHERE pzn = '10000001'),
        '1 Tablette', DATE '2026-07-01', NULL, TRUE),
    ((SELECT id FROM patient WHERE svnr = '2345020285'), (SELECT id FROM drug WHERE name = 'Metformin'),
        '1 Tablette', DATE '2026-06-01', NULL, TRUE),
    ((SELECT id FROM patient WHERE svnr = '3456030380'), (SELECT id FROM drug WHERE name = 'Omeprazole'),
        '1 Kapsel', DATE '2026-07-15', NULL, TRUE);

-- Anna Gruber (Aspirin): once daily.
INSERT INTO medication_schedule_time (schedule_id, time_of_day)
SELECT id, TIME '08:00' FROM medication_schedule
WHERE patient_id = (SELECT id FROM patient WHERE svnr = '1234010190');

-- Max Bauer (Metformin): twice daily - demonstrates a schedule with more than one time slot.
INSERT INTO medication_schedule_time (schedule_id, time_of_day)
SELECT id, TIME '08:00' FROM medication_schedule
WHERE patient_id = (SELECT id FROM patient WHERE svnr = '2345020285')
UNION ALL
SELECT id, TIME '20:00' FROM medication_schedule
WHERE patient_id = (SELECT id FROM patient WHERE svnr = '2345020285');

-- Lena Hofer (Omeprazole): once daily, before breakfast.
INSERT INTO medication_schedule_time (schedule_id, time_of_day)
SELECT id, TIME '07:00' FROM medication_schedule
WHERE patient_id = (SELECT id FROM patient WHERE svnr = '3456030380');

-- A few days of intake history for Anna Gruber's 08:00 slot (mostly taken, one skipped) so the
-- medication-plan view has real history to show immediately, without requiring manual confirms
-- first. Today itself is deliberately left with no row, so it shows as still-pending/due.
INSERT INTO medication_intake_log (schedule_time_id, scheduled_date, status, confirmed_at)
SELECT mst.id, DATE '2026-07-25', 'TAKEN', '2026-07-25 08:05:00+02'::timestamptz
FROM medication_schedule_time mst
JOIN medication_schedule ms ON ms.id = mst.schedule_id
WHERE ms.patient_id = (SELECT id FROM patient WHERE svnr = '1234010190');

INSERT INTO medication_intake_log (schedule_time_id, scheduled_date, status, confirmed_at)
SELECT mst.id, DATE '2026-07-26', 'SKIPPED', '2026-07-26 09:30:00+02'::timestamptz
FROM medication_schedule_time mst
JOIN medication_schedule ms ON ms.id = mst.schedule_id
WHERE ms.patient_id = (SELECT id FROM patient WHERE svnr = '1234010190');

INSERT INTO medication_intake_log (schedule_time_id, scheduled_date, status, confirmed_at)
SELECT mst.id, DATE '2026-07-27', 'TAKEN', '2026-07-27 08:02:00+02'::timestamptz
FROM medication_schedule_time mst
JOIN medication_schedule ms ON ms.id = mst.schedule_id
WHERE ms.patient_id = (SELECT id FROM patient WHERE svnr = '1234010190');
