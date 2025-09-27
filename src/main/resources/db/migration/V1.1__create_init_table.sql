CREATE TABLE IF NOT EXISTS file_group
(
    identifier    UUID                     NOT NULL,
    name          VARCHAR                  NOT NULL,
    type          VARCHAR                  NOT NULL,
    thumbnail     VARCHAR                  NULL,
    created_date  TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_date TIMESTAMP WITH TIME ZONE NULL,
    PRIMARY KEY (identifier)
);

CREATE TABLE IF NOT EXISTS file
(
    identifier             UUID                     NOT NULL,
    file_group_identifier  UUID                     NOT NULL,
    name                   VARCHAR                  NOT NULL,
    type                   VARCHAR                  NOT NULL,
    path                   VARCHAR                  NOT NULL,
    thumbnail              VARCHAR                  NULL,
    sort_order             INTEGER                  NOT NULL,
    created_date           TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_date          TIMESTAMP WITH TIME ZONE NULL,
    last_view_date         TIMESTAMP WITH TIME ZONE NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (file_group_identifier) REFERENCES file_group (identifier)
);

CREATE TABLE IF NOT EXISTS file_hash
(
    identifier      UUID    NOT NULL,
    file_identifier UUID    NOT NULL,
    hash            VARCHAR NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (file_identifier) REFERENCES file (identifier)
);

CREATE TABLE IF NOT EXISTS tag
(
    identifier UUID    NOT NULL,
    name       VARCHAR NOT NULL,
    type       VARCHAR NOT NULL,
    PRIMARY KEY (identifier)
);

CREATE TABLE IF NOT EXISTS file_tag
(
    identifier      UUID NOT NULL,
    file_identifier UUID NOT NULL,
    tag_identifier  UUID NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (file_identifier) REFERENCES file (identifier),
    FOREIGN KEY (tag_identifier) REFERENCES tag (identifier)
);

CREATE TABLE IF NOT EXISTS file_group_tag
(
    identifier             UUID NOT NULL,
    file_group_identifier  UUID NOT NULL,
    tag_identifier         UUID NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (file_group_identifier) REFERENCES file_group (identifier),
    FOREIGN KEY (tag_identifier) REFERENCES tag (identifier)
);

CREATE TABLE IF NOT EXISTS unsorted_file
(
    identifier UUID         NOT NULL,
    path       VARCHAR      NOT NULL,
    name       VARCHAR      NOT NULL,
    file_hash  VARCHAR(64)  NULL,
    encoding   VARCHAR      NULL,
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
