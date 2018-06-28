CREATE TABLE recruteurs_peconnect
(
  id             BIGSERIAL              NOT NULL,
  recruteur_id CHARACTER VARYING(255) NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT recruteurs_peconnect_pk PRIMARY KEY (id),
  UNIQUE (recruteur_id, peconnect_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE recruteurs_peconnect OWNER TO perspectives;
COMMENT ON TABLE recruteurs_peconnect IS 'Table de relation entre les identifiants recruteurs et PEConnect';
COMMENT ON COLUMN recruteurs_peconnect.recruteur_id IS 'Identifiant unique du recruteur';
COMMENT ON COLUMN recruteurs_peconnect.peconnect_id IS 'Identifiant unique PEConnect du recruteur';