-- Idealement, devrait etre un systeme externe interrogeable, car renseigné par un extract issu d'un SI externe à perspectives
CREATE TABLE mrs_validees
(
  id             BIGSERIAL              NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  code_metier CHARACTER VARYING(255) NOT NULL,
  date_evaluation DATE NOT NULL,
  CONSTRAINT mrs_validees_pk PRIMARY KEY (id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE mrs_validees OWNER TO perspectives;
COMMENT ON TABLE mrs_validees IS 'Table referentielle contenant les MRS validées par identifiant de Candidat PEConnect';
COMMENT ON COLUMN mrs_validees.peconnect_id IS 'Identifiant unique PEConnect du candidat';
COMMENT ON COLUMN mrs_validees.code_metier IS 'Code ROME du metier validé';
COMMENT ON COLUMN mrs_validees.date_evaluation IS 'Date de l evaluation';

CREATE INDEX peconnect_id_idx ON mrs_validees (peconnect_id);