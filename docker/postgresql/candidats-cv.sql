CREATE TABLE candidats_cv
(
  id             BIGSERIAL              NOT NULL,
  candidat_id CHARACTER VARYING(255) NOT NULL,
  cv_id   CHARACTER VARYING(255) NOT NULL,
  nom_fichier   CHARACTER VARYING(255) NOT NULL,
  fichier bytea NOT NULL,
  type_media   CHARACTER VARYING(255) NOT NULL,
  hash   CHARACTER VARYING(255) NOT NULL,
  date TIMESTAMP with time zone,
  CONSTRAINT candidats_cv_pk PRIMARY KEY (id),
  UNIQUE (candidat_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_cv OWNER TO perspectives;
COMMENT ON TABLE candidats_cv IS 'Table de relation entre les candidats et leur cv';
COMMENT ON COLUMN candidats_cv.candidat_id IS 'Identifiant unique du candidat';
COMMENT ON COLUMN candidats_cv.cv_id IS 'Identifiant unique du CV';
