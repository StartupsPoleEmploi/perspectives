CREATE TABLE candidats_disponibilite
(
  id             BIGSERIAL              NOT NULL,
  candidat_id   CHARACTER VARYING(255) NOT NULL,
  date_dernier_envoi_mail DATE NOT NULL,
  CONSTRAINT candidats_disponibilite_pk PRIMARY KEY (id),
  UNIQUE (candidat_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_disponibilite OWNER TO perspectives;
COMMENT ON TABLE candidats_disponibilite IS 'Table permettant de stocker la date de dernier envoi du mail de disponibilites par identifiant de Candidat';
COMMENT ON COLUMN candidats_disponibilite.candidat_id IS 'Identifiant unique du candidat';
COMMENT ON COLUMN candidats_disponibilite.date_dernier_envoi_mail IS 'Date de dernier envoi du mail de dispo';
