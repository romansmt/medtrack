ALTER TABLE patient ADD COLUMN email VARCHAR(255);

-- Only set for accounts created via the standard (email/password) registration path - the 4 seeded
-- demo patients and any ID-Austria-only accounts keep this null. Partial unique index so multiple
-- nulls are allowed (needed for exactly that reason).
CREATE UNIQUE INDEX uq_patient_email ON patient (email) WHERE email IS NOT NULL;
