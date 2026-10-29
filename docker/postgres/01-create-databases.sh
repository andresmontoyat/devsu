#!/bin/sh
# Database per service: each service owns its schema and never reads the other's tables.
# Two roles per database: <svc>_owner runs the Liquibase DDL, <svc>_app only reads and writes rows.
set -eu

create_database() {
  db=$1 owner=$2 owner_password=$3 app=$4 app_password=$5

  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
    -v db="$db" -v owner="$owner" -v owner_password="$owner_password" \
    -v app="$app" -v app_password="$app_password" <<'SQL'
CREATE ROLE :"owner" LOGIN PASSWORD :'owner_password';
CREATE ROLE :"app" LOGIN PASSWORD :'app_password';
CREATE DATABASE :"db" OWNER :"owner";
REVOKE ALL ON DATABASE :"db" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"db" TO :"app";
SQL

  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" \
    -v owner="$owner" -v app="$app" <<'SQL'
GRANT USAGE ON SCHEMA public TO :"app";
-- Tables created later by Liquibase (as the owner) are granted to the app role automatically.
ALTER DEFAULT PRIVILEGES FOR ROLE :"owner" IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"app";
ALTER DEFAULT PRIVILEGES FOR ROLE :"owner" IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO :"app";
SQL
}

create_database cliente_db cliente_owner "$CLIENTE_DB_OWNER_PASSWORD" cliente_app "$CLIENTE_DB_APP_PASSWORD"
create_database cuenta_db cuenta_owner "$CUENTA_DB_OWNER_PASSWORD" cuenta_app "$CUENTA_DB_APP_PASSWORD"
