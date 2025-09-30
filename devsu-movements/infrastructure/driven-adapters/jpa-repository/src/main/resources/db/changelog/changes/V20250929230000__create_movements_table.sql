-- liquibase formatted sql
-- changeset andresmontoyat:20250930000002
-- comment: Create movements table

CREATE TABLE movements (
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    account_id uuid NOT NULL,
    movement_date timestamp NOT NULL,
    value numeric(19,2) NOT NULL,
    balance numeric(19,2) NOT NULL,
    CONSTRAINT movements_pkey PRIMARY KEY (id)
);

-- rollback DROP TABLE movements;
