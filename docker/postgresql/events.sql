CREATE TABLE events
(
  id bigserial NOT NULL,
  stream_version integer NOT NULL,
  stream_name character varying(255) NOT NULL,
  event_data jsonb NOT NULL,
  event_type character varying(255) NOT NULL,
  CONSTRAINT events_pk PRIMARY KEY (id),
  UNIQUE (stream_version, stream_name)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE events OWNER TO perspectives;
COMMENT ON TABLE events IS 'Contient les événements';
COMMENT ON COLUMN events.stream_version IS 'version du stream (incrémentale)';
COMMENT ON COLUMN events.stream_name IS 'Identifiant de l''aggregat concerne par l''événement';
COMMENT ON COLUMN events.event_data IS 'Données de l''événement serialisé en JSONB';

CREATE INDEX events_stream_name_idx ON events (stream_name);