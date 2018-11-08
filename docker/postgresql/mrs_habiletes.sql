CREATE TABLE mrs_habiletes
(
  id             BIGSERIAL NOT NULL,
  code_rome CHARACTER VARYING(255) NOT NULL,
  code_departement   CHARACTER VARYING(255) NOT NULL,
  habiletes   TEXT[] DEFAULT '{}',
  CONSTRAINT mrs_habiletes_pk PRIMARY KEY (id),
  UNIQUE (code_rome, code_departement)
)
WITH (
OIDS =FALSE
);
ALTER TABLE mrs_habiletes OWNER TO perspectives;
COMMENT ON TABLE mrs_habiletes IS 'Table contenant les habiletés associés à la combinaison (code_rome, code_departement)';
COMMENT ON COLUMN mrs_habiletes.code_rome IS 'Code ROME du metier';
COMMENT ON COLUMN mrs_habiletes.code_departement IS 'Code département : les habiletés diffèrent par département';