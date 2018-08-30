-- Idealement, devrait etre un systeme externe interrogeable, car renseigné par un extract issu du SI Pole Emploi
CREATE TABLE mrs_validees
(
  id             BIGSERIAL              NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  code_rome CHARACTER VARYING(255) NOT NULL,
  date_evaluation DATE NOT NULL,
  CONSTRAINT mrs_validees_pk PRIMARY KEY (id),
  UNIQUE (peconnect_id, code_rome, date_evaluation)
)
WITH (
OIDS =FALSE
);
ALTER TABLE mrs_validees OWNER TO perspectives;
COMMENT ON TABLE mrs_validees IS 'Table referentielle contenant les MRS validées par identifiant de Candidat PEConnect';
COMMENT ON COLUMN mrs_validees.peconnect_id IS 'Identifiant unique PEConnect du candidat';
COMMENT ON COLUMN mrs_validees.code_rome IS 'Code ROME du metier validé';
COMMENT ON COLUMN mrs_validees.date_evaluation IS 'Date de l evaluation';

CREATE INDEX peconnect_id_idx ON mrs_validees (peconnect_id);