CREATE TABLE mrs_habiletes
(
  id          BIGSERIAL NOT NULL,
  code_rome   CHARACTER VARYING(255) NOT NULL,
  habiletes   TEXT[] DEFAULT '{}',
  CONSTRAINT mrs_habiletes_pk PRIMARY KEY (id),
  UNIQUE (code_rome)
)
WITH (
OIDS =FALSE
);
ALTER TABLE mrs_habiletes OWNER TO perspectives;
COMMENT ON TABLE mrs_habiletes IS 'Table contenant les habiletés associées à un codeROME';
COMMENT ON COLUMN mrs_habiletes.code_rome IS 'Code ROME du metier';
COMMENT ON COLUMN mrs_habiletes.habiletes IS 'Habiletés associées au métier';