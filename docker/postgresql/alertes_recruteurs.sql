CREATE TABLE alertes_recruteurs
(
  id             BIGSERIAL              NOT NULL,
  recruteur_id CHARACTER VARYING(255) NOT NULL,
  alerte_id CHARACTER VARYING(255) NOT NULL,
  prenom_recruteur   CHARACTER VARYING(255) NOT NULL,
  type_recruteur   CHARACTER VARYING(255) NOT NULL,
  email_recruteur   CHARACTER VARYING(255) NOT NULL,
  frequence CHARACTER VARYING(255) NOT NULL,
  metier   CHARACTER VARYING(255),
  secteur_activite   CHARACTER VARYING(255),
  departement   CHARACTER VARYING(255),
  CONSTRAINT alertes_recruteurs_pk PRIMARY KEY (id),
  UNIQUE (recruteur_id, alerte_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE alertes_recruteurs OWNER TO perspectives;
COMMENT ON TABLE alertes_recruteurs IS 'Table des alertes des recruteurs';
COMMENT ON COLUMN alertes_recruteurs.recruteur_id IS 'Identifiant unique du recruteur';
COMMENT ON COLUMN alertes_recruteurs.alerte_id IS 'Identifiant unique de l''alerte';

CREATE INDEX alerte_id_idx ON alertes_recruteurs (alerte_id);
CREATE INDEX alerte_recruteur_id_idx ON alertes_recruteurs (recruteur_id);