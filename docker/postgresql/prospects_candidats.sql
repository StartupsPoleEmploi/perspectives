CREATE TABLE prospects_candidats
(
  id             BIGSERIAL              NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  identifiant_local CHARACTER VARYING(255) NOT NULL,
  nom   CHARACTER VARYING(255) NOT NULL,
  prenom   CHARACTER VARYING(255) NOT NULL,
  email   CHARACTER VARYING(255) NOT NULL,
  genre   CHARACTER VARYING(255) NOT NULL,
  code_departement   CHARACTER VARYING(255) NOT NULL,
  code_rome_mrs   CHARACTER VARYING(255) NOT NULL,
  metier_mrs   CHARACTER VARYING(255) NOT NULL,
  date_evaluation_mrs DATE NOT NULL,
  CONSTRAINT prospects_candidats_pk PRIMARY KEY (id),
  UNIQUE (peconnect_id, identifiant_local, code_rome_mrs)
)
WITH (
OIDS =FALSE
);
ALTER TABLE prospects_candidats OWNER TO perspectives;
COMMENT ON TABLE prospects_candidats IS 'Table des prospects candidats';
