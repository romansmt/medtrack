ALTER TABLE patient ADD COLUMN medtrack_id VARCHAR(20);

-- MedTrack's own internal account/customer number - distinct from the SVNR (the government-issued
-- social insurance number). A real system would generate these at account-creation time; here they're
-- just fixed per seeded demo patient, the same way every other fake identifier in this project is.
UPDATE patient SET medtrack_id = 'MT-10000001' WHERE svnr = '1234010190';
UPDATE patient SET medtrack_id = 'MT-10000002' WHERE svnr = '2345020285';
UPDATE patient SET medtrack_id = 'MT-10000003' WHERE svnr = '3456030380';
UPDATE patient SET medtrack_id = 'MT-10000004' WHERE svnr = '4567040475';

ALTER TABLE patient ALTER COLUMN medtrack_id SET NOT NULL;
ALTER TABLE patient ADD CONSTRAINT uq_patient_medtrack_id UNIQUE (medtrack_id);
