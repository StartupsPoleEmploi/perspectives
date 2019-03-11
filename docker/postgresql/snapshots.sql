CREATE TABLE snapshots
(
  id bigserial NOT NULL,
  stream_name character varying(255) NOT NULL,
  stream_version integer NOT NULL,
  stream_type character varying(255) NOT NULL,
  snapshot_data jsonb NOT NULL,
  CONSTRAINT snapshots_pk PRIMARY KEY (id),
  UNIQUE (stream_name)
)
  WITH (
    OIDS=FALSE
  );
ALTER TABLE snapshots OWNER TO perspectives;
COMMENT ON TABLE snapshots IS 'Contient les sauvegardes de l''état interne d''un aggregat pour une version';
COMMENT ON COLUMN snapshots.stream_name IS 'Identifiant de l''aggregat';
COMMENT ON COLUMN snapshots.stream_version IS 'version de l''aggregat correspondant aux données du snapshot';
COMMENT ON COLUMN snapshots.stream_type IS 'Nom du type de l''aggregat represente';
COMMENT ON COLUMN snapshots.snapshot_data IS 'Etat de l''aggrégat serialisé en JSONB';

CREATE INDEX snapshots_stream_name_idx ON snapshots(stream_name);