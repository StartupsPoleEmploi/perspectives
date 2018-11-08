-- Idealement, devrait etre un systeme externe interrogeable, car renseigné par un extract issu du SI Pole Emploi
CREATE TABLE candidats_mrs_validees
(
  id             BIGSERIAL              NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  code_rome CHARACTER VARYING(255) NOT NULL,
  code_departement CHARACTER VARYING(255) NOT NULL,
  date_evaluation DATE NOT NULL,
  CONSTRAINT candidats_mrs_validees_pk PRIMARY KEY (id),
  UNIQUE (peconnect_id, code_rome)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_mrs_validees OWNER TO perspectives;
COMMENT ON TABLE candidats_mrs_validees IS 'Table referentielle contenant les MRS validées par identifiant de Candidat PEConnect';
COMMENT ON COLUMN candidats_mrs_validees.peconnect_id IS 'Identifiant unique PEConnect du candidat';
COMMENT ON COLUMN candidats_mrs_validees.code_rome IS 'Code ROME du metier validé';
COMMENT ON COLUMN candidats_mrs_validees.date_evaluation IS 'Date de l evaluation';

CREATE INDEX candidats_mrs_validees_peconnect_id_idx ON candidats_mrs_validees (peconnect_id);