-- liquibase formatted sql
-- changeset andresmontoyat:20250930000001
-- comment: Create accounts table

CREATE TABLE accounts (
    id uuid NOT NULL,
    account_number bigint NOT NULL,
    account_type varchar(255) NOT NULL,
    initial_balance numeric(19,2) NOT NULL,
    status boolean NOT NULL,
    customer_id uuid NOT NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id)
);

-- rollback DROP TABLE accounts;
