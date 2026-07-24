-- Baseline migration: proves Flyway is wired to the database.
-- Feature migrations start at V2__ and live alongside this file until
-- per-feature migration folders are introduced (see CLAUDE.md).
CREATE TABLE IF NOT EXISTS schema_metadata (
    id SMALLINT PRIMARY KEY DEFAULT 1,
    initialized_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT schema_metadata_singleton CHECK (id = 1)
);

INSERT INTO schema_metadata (id) VALUES (1)
ON CONFLICT (id) DO NOTHING;
