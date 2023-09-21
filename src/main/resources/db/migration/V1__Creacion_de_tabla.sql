CREATE TABLE emails (
    email_id bigserial PRIMARY KEY,
    email_from VARCHAR(255) NOT NULL,
    email_to VARCHAR(1000) NOT NULL,
    email_cc VARCHAR(1000),
    email_body TEXT,
    email_state INT,
    update_date timestamp DEFAULT current_timestamp
);