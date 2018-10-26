CREATE TABLE recruteurs_mailjet
(
  id             BIGSERIAL              NOT NULL,
  recruteur_id CHARACTER VARYING(255) NOT NULL,
  mailjet_id   BIGINT NOT NULL,
  email   CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT recruteurs_mailjet_pk PRIMARY KEY (id),
  UNIQUE (recruteur_id, mailjet_id)
)
WITH (
OIDS =FALSE
);
ALTER TABLE recruteurs_mailjet OWNER TO perspectives;
COMMENT ON TABLE recruteurs_mailjet IS 'Table de relation entre les identifiants recruteurs et Mailjet';
COMMENT ON COLUMN recruteurs_mailjet.recruteur_id IS 'Identifiant unique du recruteur';
COMMENT ON COLUMN recruteurs_mailjet.mailjet_id IS 'Identifiant unique Mailjet du recruteur';