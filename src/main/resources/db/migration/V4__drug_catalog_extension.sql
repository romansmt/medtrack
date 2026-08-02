ALTER TABLE drug ADD COLUMN form VARCHAR(255);
ALTER TABLE drug ADD COLUMN pack_size VARCHAR(50);
ALTER TABLE drug ADD COLUMN manufacturer VARCHAR(255);
ALTER TABLE drug ADD COLUMN prescription_required BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE drug ADD COLUMN pzn VARCHAR(8);
ALTER TABLE drug ADD COLUMN package_leaflet_text TEXT;

UPDATE drug SET form = 'Tabletten', pack_size = '30 ST', manufacturer = 'Bayer Austria GmbH',
    prescription_required = FALSE, pzn = '10000001',
    package_leaflet_text = 'Acetylsalicylic acid 500mg. For short-term relief of mild to moderate pain and fever. Simulated leaflet text - not medical advice.'
    WHERE name = 'Aspirin';

UPDATE drug SET form = 'Tabletten', pack_size = '20 ST', manufacturer = 'Reckitt Benckiser GmbH',
    prescription_required = FALSE, pzn = '10000002',
    package_leaflet_text = 'Ibuprofen 400mg. Nonsteroidal anti-inflammatory for pain and inflammation. Simulated leaflet text - not medical advice.'
    WHERE name = 'Nurofen';

UPDATE drug SET form = 'Tabletten', pack_size = '20 ST', manufacturer = 'Genericon Pharma GmbH',
    prescription_required = FALSE, pzn = '10000003',
    package_leaflet_text = 'Paracetamol 500mg. For mild to moderate pain and fever. Simulated leaflet text - not medical advice.'
    WHERE name = 'Paracetamol';

UPDATE drug SET form = 'Filmtabletten', pack_size = '20 ST', manufacturer = 'Sandoz GmbH',
    prescription_required = TRUE, pzn = '10000004',
    package_leaflet_text = 'Amoxicillin 500mg. Penicillin antibiotic - complete the full course as prescribed. Simulated leaflet text - not medical advice.'
    WHERE name = 'Amoxicillin';

UPDATE drug SET form = 'Filmtabletten', pack_size = '60 ST', manufacturer = 'Ratiopharm Arzneimittel Vertriebs GmbH',
    prescription_required = TRUE, pzn = '10000005',
    package_leaflet_text = 'Metformin 850mg. Oral antidiabetic - take with meals. Simulated leaflet text - not medical advice.'
    WHERE name = 'Metformin';

UPDATE drug SET form = 'Kapseln', pack_size = '14 ST', manufacturer = 'Fresenius Kabi Austria GmbH',
    prescription_required = TRUE, pzn = '10000006',
    package_leaflet_text = 'Omeprazole 20mg. Proton pump inhibitor for acid reflux. Simulated leaflet text - not medical advice.'
    WHERE name = 'Omeprazole';

ALTER TABLE drug ALTER COLUMN form SET NOT NULL;
ALTER TABLE drug ALTER COLUMN pack_size SET NOT NULL;
ALTER TABLE drug ALTER COLUMN manufacturer SET NOT NULL;
ALTER TABLE drug ALTER COLUMN pzn SET NOT NULL;
ALTER TABLE drug ADD CONSTRAINT uq_drug_pzn UNIQUE (pzn);

-- Additional catalog rows: same active substance as an existing drug but a different pack size or
-- manufacturer, matching how real medication-availability search results list several package
-- variants under one search term. Also adds a couple of new active substances for catalog variety.
INSERT INTO drug (name, active_substance, form, pack_size, manufacturer, prescription_required, pzn, package_leaflet_text) VALUES
    ('Paracetamol', 'Paracetamol', 'Tabletten', '10 ST', 'Genericon Pharma GmbH', FALSE, '10000007',
     'Paracetamol 500mg. For mild to moderate pain and fever. Simulated leaflet text - not medical advice.'),
    ('Paracetamol Kabi', 'Paracetamol', 'Infusionsloesung', '10 ST', 'Fresenius Kabi Austria GmbH', TRUE, '10000008',
     'Paracetamol 10mg/ml infusion solution. Hospital-style use for pain and fever when oral dosing is not possible. Simulated leaflet text - not medical advice.'),
    ('Ibuprofen Ratiopharm', 'Ibuprofen', 'Filmtabletten', '50 ST', 'Ratiopharm Arzneimittel Vertriebs GmbH', FALSE, '10000009',
     'Ibuprofen 400mg. Nonsteroidal anti-inflammatory for pain and inflammation. Simulated leaflet text - not medical advice.'),
    ('Vitamin C Genericon', 'Ascorbic acid', 'Brausetabletten', '20 ST', 'Genericon Pharma GmbH', FALSE, '10000010',
     'Vitamin C 1000mg effervescent tablets. Dietary supplement. Simulated leaflet text - not medical advice.'),
    ('Loratadin Sandoz', 'Loratadine', 'Tabletten', '10 ST', 'Sandoz GmbH', FALSE, '10000011',
     'Loratadine 10mg. Antihistamine for allergy symptoms. Simulated leaflet text - not medical advice.'),
    ('Pantoprazole Ratiopharm', 'Pantoprazole', 'Filmtabletten', '30 ST', 'Ratiopharm Arzneimittel Vertriebs GmbH', TRUE, '10000012',
     'Pantoprazole 40mg. Proton pump inhibitor for acid reflux. Simulated leaflet text - not medical advice.');
