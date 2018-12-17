CREATE TABLE recruteurs_alertes
(
  id             BIGSERIAL              NOT NULL,
  recruteur_id CHARACTER VARYING(255) NOT NULL,
  alerte_id CHARACTER VARYING(255) NOT NULL,
  type_recruteur   CHARACTER VARYING(255) NOT NULL,
  email_recruteur   CHARACTER VARYING(255) NOT NULL,
  frequence CHARACTER VARYING(255) NOT NULL,
  metier   CHARACTER VARYING(255),
  secteur_activite   CHARACTER VARYING(255),
  label_localisation   CHARACTER VARYING(255),
  latitude   NUMERIC(11, 8),
  longitude   NUMERIC(11, 8),
  CONSTRAINT recruteurs_alertes_pk PRIMARY KEY (id),
  UNIQUE (recruteur_id, alerte_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE recruteurs_alertes OWNER TO perspectives;
COMMENT ON TABLE recruteurs_alertes IS 'Table des alertes des recruteurs';
COMMENT ON COLUMN recruteurs_alertes.recruteur_id IS 'Identifiant unique du recruteur';
COMMENT ON COLUMN recruteurs_alertes.alerte_id IS 'Identifiant unique de l''alerte';

CREATE INDEX recruteurs_alertes_alerte_id_idx ON recruteurs_alertes (alerte_id);
CREATE INDEX recruteurs_alertes_recruteur_id_idx ON recruteurs_alertes (recruteur_id);