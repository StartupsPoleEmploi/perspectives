CREATE TABLE candidats_peconnect
(
  id             BIGSERIAL              NOT NULL,
  candidat_id CHARACTER VARYING(255) NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT candidats_peconnect_pk PRIMARY KEY (id),
  UNIQUE (candidat_id, peconnect_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_peconnect OWNER TO perspectives;
COMMENT ON TABLE candidats_peconnect IS 'Table de relation entre les identifiants candidats et PEConnect';
COMMENT ON COLUMN candidats_peconnect.candidat_id IS 'Identifiant unique du candidat';
COMMENT ON COLUMN candidats_peconnect.peconnect_id IS 'Identifiant unique PEConnect du candidat';