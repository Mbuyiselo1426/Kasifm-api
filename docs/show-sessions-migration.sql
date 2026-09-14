-- Run once against Neon/PostgreSQL before deploying the ShowSession feature.
-- Existing messages are deliberately retained with show_session_id = NULL.

CREATE TABLE IF NOT EXISTS show_sessions (
    id BIGSERIAL PRIMARY KEY,
    show_id BIGINT NOT NULL REFERENCES shows(id),
    session_date DATE NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ends_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_show_sessions_show_date UNIQUE (show_id, session_date)
);

ALTER TABLE messages ADD COLUMN IF NOT EXISTS show_session_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_messages_show_session') THEN
        ALTER TABLE messages
            ADD CONSTRAINT fk_messages_show_session
            FOREIGN KEY (show_session_id) REFERENCES show_sessions(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_messages_show_session_created_at
    ON messages (show_session_id, created_at DESC, id DESC);
