CREATE TABLE candidats_mrs_dhae_validees
(
  id             BIGSERIAL              NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  code_rome CHARACTER VARYING(255) NOT NULL,
  code_departement CHARACTER VARYING(255) NOT NULL,
  date_evaluation DATE NOT NULL,
  CONSTRAINT candidats_mrs_dhae_validees_pk PRIMARY KEY (id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_mrs_dhae_validees OWNER TO perspectives;
COMMENT ON TABLE candidats_mrs_dhae_validees IS 'Table referentielle contenant les MRS DHAE validées par identifiant de Candidat PEConnect';
COMMENT ON COLUMN candidats_mrs_dhae_validees.peconnect_id IS 'Identifiant unique PEConnect du candidat';
COMMENT ON COLUMN candidats_mrs_dhae_validees.code_rome IS 'Code ROME du metier validé';
COMMENT ON COLUMN candidats_mrs_dhae_validees.date_evaluation IS 'Date de l evaluation';

CREATE INDEX candidats_mrs_dhae_validees_peconnect_id_idx ON candidats_mrs_dhae_validees (peconnect_id);
ALTER TABLE candidats_mrs_dhae_validees ADD CONSTRAINT candidats_mrs_dhae_validees_unicite_mrs UNIQUE (peconnect_id, code_rome, code_departement);