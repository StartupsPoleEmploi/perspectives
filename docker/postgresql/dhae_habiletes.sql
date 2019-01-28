CREATE TABLE dhae_habiletes
(
  id             BIGSERIAL NOT NULL,
  code_rome CHARACTER VARYING(255) NOT NULL,
  code_departement   CHARACTER VARYING(255) NOT NULL,
  habiletes   TEXT[] DEFAULT '{}',
  CONSTRAINT dhae_habiletes_pk PRIMARY KEY (id),
  UNIQUE (code_rome, code_departement)
)
WITH (
OIDS =FALSE
);
ALTER TABLE dhae_habiletes OWNER TO perspectives;
COMMENT ON TABLE dhae_habiletes IS 'Table contenant les habiletés associées à la combinaison (code_rome, code_departement) pour les DHAE';
COMMENT ON COLUMN dhae_habiletes.code_rome IS 'Code ROME du metier';
COMMENT ON COLUMN dhae_habiletes.code_departement IS 'Code département : les habiletés diffèrent par département';