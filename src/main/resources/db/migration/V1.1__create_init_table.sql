CREATE TABLE IF NOT EXISTS media_file
(
    identifier       UUID    NOT NULL,
    type             VARCHAR NOT NULL,
    group_identifier UUID,
    hash             VARCHAR NOT NULL,
    name             VARCHAR NOT NULL,
    path             VARCHAR NOT NULL,
    tag              VARCHAR NOT NULL,
    PRIMARY KEY (identifier)
);

CREATE TABLE IF NOT EXISTS "user"
(
    identifier  UUID                     NOT NULL,
    username    VARCHAR(255)             NOT NULL,
    password    VARCHAR(255)             NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier)
);

CREATE TABLE IF NOT EXISTS task_queue
(
    identifier    UUID        NOT NULL,
    type          VARCHAR(50) NOT NULL,
    payload       JSONB       NOT NULL,
    status        VARCHAR(20) NOT NULL,
    created_at    TIMESTAMP   NOT NULL,
    processed_at  TIMESTAMP   NULL,
    error_message TEXT        NULL,
    retry_count   INTEGER     NOT NULL,
    PRIMARY KEY (identifier)
);

CREATE INDEX IF NOT EXISTS idx_outbox_event_status_created_at ON task_queue (status, created_at);
CREATE INDEX IF NOT EXISTS idx_outbox_event_event_type ON task_queue (type);
