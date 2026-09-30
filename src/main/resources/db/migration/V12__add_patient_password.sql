-- MedTrack's own login credential, distinct from ID Austria's identity data (id_austria_registry).
-- Null until a patient completes the ID-Austria registration flow and sets a password - see
-- DemoAccountSeeder for why the seeded demo patients' hashes are filled in by the app at startup
-- rather than as literal values here.
ALTER TABLE patient ADD COLUMN password_hash VARCHAR(255);
