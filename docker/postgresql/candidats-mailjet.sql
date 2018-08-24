CREATE TABLE candidats_mailjet
(
  id             BIGSERIAL              NOT NULL,
  candidat_id CHARACTER VARYING(255) NOT NULL,
  mailjet_id   BIGINT NOT NULL,
  email   CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT candidats_mailjet_pk PRIMARY KEY (id),
  UNIQUE (candidat_id, mailjet_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE candidats_mailjet OWNER TO perspectives;
COMMENT ON TABLE candidats_mailjet IS 'Table de relation entre les identifiants candidats et Mailjet';
COMMENT ON COLUMN candidats_mailjet.candidat_id IS 'Identifiant unique du candidat';
COMMENT ON COLUMN candidats_mailjet.mailjet_id IS 'Identifiant unique Mailjet du candidat';