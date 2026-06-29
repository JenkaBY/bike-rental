#!/usr/bin/env bash
set -Eeuo pipefail

# Creates every application database (the primary POSTGRES_DB plus any space-
# separated names in EXTRA_DATABASES) and applies the identical privilege model
# to each. Runs once on first cluster init, after 01-roles.sh. Safe to re-run by
# hand against an existing cluster (CREATE is guarded, GRANTs are idempotent).

DATABASES="${POSTGRES_DB} ${EXTRA_DATABASES:-}"

apply_grants() {
    local db="$1"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$db" -v dbname="$db" <<'EOSQL'
-- Admin owns the database and the public schema.
ALTER DATABASE :"dbname" OWNER TO bikerental_admin;
ALTER SCHEMA public OWNER TO bikerental_admin;

-- Start from a closed schema, then grant deliberately.
REVOKE ALL ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE :"dbname"
    TO bikerental_admin, bikerental_liquibase, bikerental_app, bikerental_app_migrator;

-- DDL roles may create objects in public.
GRANT USAGE, CREATE ON SCHEMA public
    TO bikerental_admin, bikerental_liquibase, bikerental_app_migrator;

-- Runtime role: schema usage only. DML comes from default privileges below.
GRANT USAGE ON SCHEMA public TO bikerental_app;

-- Objects created by liquibase: app reads/writes, admin full.
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT ALL ON TABLES TO bikerental_admin;

-- Objects created by app_migrator: app reads/writes, admin full.
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT ALL ON TABLES TO bikerental_admin;

-- bikerental_admin owns the DB, so Liquibase migrations running as that role
-- create tables owned by it. Grant app_migrator and app access to objects it
-- creates, both existing and future.
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_admin IN SCHEMA public
    GRANT ALL ON TABLES TO bikerental_app_migrator;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_admin IN SCHEMA public
    GRANT ALL ON SEQUENCES TO bikerental_app_migrator;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_admin IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_admin IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bikerental_app;
EOSQL
}

for db in $DATABASES; do
    exists="$(psql -tAc "SELECT 1 FROM pg_database WHERE datname = '${db}'" --username "$POSTGRES_USER")"
    if [ "$exists" != "1" ]; then
        echo "Creating database ${db} (owner bikerental_admin)."
        createdb --username "$POSTGRES_USER" --owner=bikerental_admin "$db"
    fi
    echo "Applying privilege model to ${db}."
    apply_grants "$db"
done
