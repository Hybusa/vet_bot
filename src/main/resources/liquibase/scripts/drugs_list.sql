-- liquibase formatted sql

-- changeset akuznetsov:1
CREATE TABLE drugs
(
    id              SERIAL PRIMARY KEY,
    name_lat        VARCHAR(255),
    name_rus        VARCHAR(255),
    type_group      varchar(255),
    dosage_for_cats VARCHAR(1000),
    dosage_for_dogs VARCHAR(1000)
);

CREATE TABLE users(
    id SERIAL PRIMARY KEY,
    chat_id bigint,
    name varchar(255),
    type_choice varchar(255)
);

INSERT INTO drugs
VALUES (0, 'acepromazine', 'ацепромазин','antibiotics',
        '• 0.025–0.1 mg/kg IM, IV, or SQ in a single dose.
        • Sedation: 1.1–2.2 mg/kg q6–8h PO.
        • Anesthetic protocols: 0.01–0.05 mg/kg IV, administered with other agents.',
        '• 0.025–0.1 mg/kg IM, IV, or SQ in a single dose (most common is 0.025 mg/kg).
        Do not exceed 3 mg total in dogs.
        • Sedation: 0.5–2.2 mg/kg q6–8h PO.
        • Anesthetic protocols: 0.01–0.05 mg/kg IV, administered with other agents.'),
       (1, 'acetazolamide', 'ацетазоламид','gastrointestinal',
        '• 7 mg/kg, q8h, PO.',
        'Note: The approved veterinary formulation lists a dose of 10–30 mg/kg for glaucoma
        and diuretic uses.
       • Glaucoma: 5–10 mg/kg q8–12h PO.
       • Other diuretic uses: 4–8 mg/kg q8–12h PO.');