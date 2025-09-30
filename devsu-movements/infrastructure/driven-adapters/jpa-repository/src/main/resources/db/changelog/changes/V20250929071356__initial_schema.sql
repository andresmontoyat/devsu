-- liquibase formatted sql
-- changeset andresmontoyat:20250929071356
-- comment: Create customer tables


CREATE TABLE customers (
  name varchar(255) NOT NULL,
  active BOOLEAN NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT customers_pkey PRIMARY KEY (id)
);

INSERT INTO customers (name, active, id) VALUES('John Doe', TRUE, 'a8adfa00-6680-49b3-bf94-caa8c3f1d823'::uuid)

-- rollback DROP TABLE customers;
