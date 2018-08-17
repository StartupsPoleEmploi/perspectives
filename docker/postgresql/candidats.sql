CREATE TABLE candidats
(
  id             BIGSERIAL              NOT NULL,
  candidat_id CHARACTER VARYING(255) NOT NULL,
  cv_id CHARACTER VARYING(255),
  nom   CHARACTER VARYING(255) NOT NULL,
  prenom   CHARACTER VARYING(255) NOT NULL,
  genre   CHARACTER VARYING(255),
  email   CHARACTER VARYING(255) NOT NULL,
  statut_demandeur_emploi   CHARACTER VARYING(255),
  code_postal   CHARACTER VARYING(255),
  commune   CHARACTER VARYING(255),
  recherche_metier_evalue BOOL,
  metiers_evalues   TEXT[] DEFAULT '{}',
  recherche_autre_metier BOOL,
  metiers_recherches TEXT[] DEFAULT '{}',
  contacte_par_agence_interim BOOL,
  contacte_par_organisme_formation BOOL,
  rayon_recherche INT,
  numero_telephone CHARACTER VARYING(255),
  date_inscription TIMESTAMP with time zone,
  indexer_matching BOOL DEFAULT FALSE,
  CONSTRAINT candidats_pk PRIMARY KEY (id),
  UNIQUE (candidat_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats OWNER TO perspectives;
COMMENT ON TABLE candidats IS 'Table des candidats';
COMMENT ON COLUMN candidats.candidat_id IS 'Identifiant unique du candidat';

CREATE UNIQUE INDEX candidat_id_idx ON candidats (candidat_id);
CREATE INDEX date_inscription_idx ON candidats (date_inscription);