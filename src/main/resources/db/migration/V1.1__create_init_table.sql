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
    identifier            UUID                     NOT NULL,
    file_group_identifier UUID                     NOT NULL,
    name                  VARCHAR                  NOT NULL,
    type                  VARCHAR                  NOT NULL,
    path                  VARCHAR                  NOT NULL,
    thumbnail             VARCHAR                  NULL,
    sort_order            INTEGER                  NOT NULL,
    created_date          TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_date         TIMESTAMP WITH TIME ZONE NULL,
    last_view_date        TIMESTAMP WITH TIME ZONE NULL,
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
    identifier            UUID NOT NULL,
    file_group_identifier UUID NOT NULL,
    tag_identifier        UUID NOT NULL,
    PRIMARY KEY (identifier),
    FOREIGN KEY (file_group_identifier) REFERENCES file_group (identifier),
    FOREIGN KEY (tag_identifier) REFERENCES tag (identifier)
);

CREATE TABLE IF NOT EXISTS unsorted_file
(
    identifier UUID        NOT NULL,
    path       VARCHAR     NOT NULL,
    name       VARCHAR     NOT NULL,
    file_hash  VARCHAR(64) NULL,
    encoding   VARCHAR     NULL,
    PRIMARY KEY (identifier)
);


CREATE TABLE IF NOT EXISTS users
(
    identifier  UUID                     NOT NULL,
    username    VARCHAR(255)             NOT NULL,
    password    VARCHAR(255)             NOT NULL,
    token       VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    modified_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (identifier)
);

CREATE TABLE IF NOT EXISTS save_task
(
    identifier            uuid                     not null,
    name                  varchar                  not null,
    path                  varchar                  not null,
    type                  varchar                  not null,
    tag_requests          jsonb                    not null,
    status                varchar                  not null,
    created_date          timestamp with time zone not null,
    modified_date         timestamp with time zone not null,
    file_group_identifier uuid,
    sort_order            integer,
    PRIMARY KEY (identifier)
);
