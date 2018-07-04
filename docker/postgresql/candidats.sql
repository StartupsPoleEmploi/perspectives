-- Table: candidats

DROP TABLE IF EXISTS candidats;

CREATE TABLE candidats
(
  id             BIGSERIAL              NOT NULL,
  candidat_id CHARACTER VARYING(255) NOT NULL,
  peconnect_id   CHARACTER VARYING(255) NOT NULL,
  nom   CHARACTER VARYING(255) NOT NULL,
  prenom   CHARACTER VARYING(255) NOT NULL,
  email   CHARACTER VARYING(255) NOT NULL,
  recherche_metier_evalue BOOL,
  recherche_autre_metier BOOL,
  metiers_recherches TEXT[],
  contacte_par_agence_interim BOOL,
  contacte_par_organisme_formation BOOL,
  rayon_recherche INT,
  date_inscription TIMESTAMP with time zone,
  CONSTRAINT candidats_pk PRIMARY KEY (id),
  UNIQUE (candidat_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats OWNER TO perspectives;
COMMENT ON TABLE candidats IS 'Table des candidats';
COMMENT ON COLUMN candidats.candidat_id IS 'Identifiant unique du candidat';
COMMENT ON COLUMN candidats.peconnect_id IS 'Identifiant unique PEConnect du candidat';