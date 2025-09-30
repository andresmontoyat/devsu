-- liquibase formatted sql
-- changeset andresmontoyat:20250925071356
-- comment: Create customer tables

CREATE TABLE persons (
    id uuid NOT NULL,
    address varchar(255) NOT NULL,
    age int4 NOT NULL,
    genre varchar(255) NOT NULL,
    identification varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    phone varchar(255) NOT NULL,
    CONSTRAINT persons_pkey PRIMARY KEY (id)
);

CREATE TABLE customers (
  client_id varchar(255) NOT NULL,
  "password" varchar(255) NOT NULL,
  active boolean NOT NULL,
  id uuid NOT NULL,
  CONSTRAINT customers_pkey PRIMARY KEY (id),
  CONSTRAINT uk_customers_client_id UNIQUE (client_id),
  CONSTRAINT fk_customers_id FOREIGN KEY (id) REFERENCES persons(id)
);

-- rollback DROP TABLE customers;
-- rollback DROP TABLE persons;
