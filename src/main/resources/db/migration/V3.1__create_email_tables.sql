-- Email Account Table
CREATE TABLE email_account (
    identifier UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL, -- 'GMAIL' or 'NAVER'
    display_name VARCHAR(255),
    
    -- OAuth2 credentials (for Gmail)
    access_token TEXT,
    refresh_token TEXT,
    token_expiry TIMESTAMP WITH TIME ZONE,
    
    -- IMAP/SMTP credentials (for Naver, encrypted)
    encrypted_password TEXT,
    
    -- Account settings
    is_active BOOLEAN DEFAULT true,
    sync_enabled BOOLEAN DEFAULT true,
    last_sync_date TIMESTAMP WITH TIME ZONE,
    
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    UNIQUE(user_id, email_address)
);

-- Email Message Table
CREATE TABLE email (
    identifier UUID PRIMARY KEY,
    email_account_identifier UUID NOT NULL REFERENCES email_account(identifier) ON DELETE CASCADE,
    
    -- Email identifiers
    message_id VARCHAR(255) NOT NULL, -- Provider's message ID
    thread_id VARCHAR(255),
    
    -- Email headers
    subject TEXT,
    sender_email VARCHAR(255),
    sender_name VARCHAR(255),
    recipient_emails TEXT[], -- Array of recipient emails
    cc_emails TEXT[],
    bcc_emails TEXT[],
    
    -- Email content
    body_text TEXT,
    body_html TEXT,
    
    -- Email metadata
    date_sent TIMESTAMP WITH TIME ZONE,
    date_received TIMESTAMP WITH TIME ZONE,
    is_read BOOLEAN DEFAULT false,
    is_starred BOOLEAN DEFAULT false,
    is_important BOOLEAN DEFAULT false,
    
    -- Folder/Label information
    folder_name VARCHAR(255), -- INBOX, SENT, DRAFT, etc.
    labels TEXT[], -- Gmail labels or custom labels
    
    -- Email size and attachments
    size_bytes BIGINT,
    has_attachments BOOLEAN DEFAULT false,
    
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    modified_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    UNIQUE(email_account_identifier, message_id)
);

-- Email Attachment Table
CREATE TABLE email_attachment (
    identifier UUID PRIMARY KEY,
    email_identifier UUID NOT NULL REFERENCES email(identifier) ON DELETE CASCADE,
    
    -- Attachment info
    filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(255),
    size_bytes BIGINT,
    content_id VARCHAR(255), -- For inline attachments
    
    -- Storage info
    stored_path TEXT, -- Path where attachment is stored
    checksum VARCHAR(64), -- SHA-256 checksum
    
    is_inline BOOLEAN DEFAULT false,
    
    created_date TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_email_account_user_id ON email_account(user_id);
CREATE INDEX idx_email_account_email_address ON email_account(email_address);
CREATE INDEX idx_email_account_provider ON email_account(provider);

CREATE INDEX idx_email_account_identifier ON email(email_account_identifier);
CREATE INDEX idx_email_message_id ON email(message_id);
CREATE INDEX idx_email_thread_id ON email(thread_id);
CREATE INDEX idx_email_sender_email ON email(sender_email);
CREATE INDEX idx_email_date_received ON email(date_received);
CREATE INDEX idx_email_is_read ON email(is_read);
CREATE INDEX idx_email_folder_name ON email(folder_name);

CREATE INDEX idx_email_attachment_email_identifier ON email_attachment(email_identifier);
CREATE INDEX idx_email_attachment_filename ON email_attachment(filename);
