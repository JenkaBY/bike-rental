#!/usr/bin/env bash
set -Eeuo pipefail

# Creates the four login roles idempotently and (re)sets their passwords from
# environment variables. Runs once on first cluster init; safe to re-run by hand.
# Passwords are passed as psql variables and emitted as quoted literals (:'var').

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" \
    -v admin_pw="$BIKERENTAL_ADMIN_PASSWORD" \
    -v liquibase_pw="$BIKERENTAL_LIQUIBASE_PASSWORD" \
    -v app_pw="$BIKERENTAL_APP_PASSWORD" \
    -v migrator_pw="$BIKERENTAL_APP_MIGRATOR_PASSWORD" <<'EOSQL'
SET password_encryption = 'scram-sha-256';

SELECT 'CREATE ROLE bikerental_admin LOGIN CREATEROLE'
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bikerental_admin')\gexec
SELECT 'CREATE ROLE bikerental_liquibase LOGIN'
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bikerental_liquibase')\gexec
SELECT 'CREATE ROLE bikerental_app LOGIN'
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bikerental_app')\gexec
SELECT 'CREATE ROLE bikerental_app_migrator LOGIN'
    WHERE NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'bikerental_app_migrator')\gexec

ALTER ROLE bikerental_admin        PASSWORD :'admin_pw';
ALTER ROLE bikerental_liquibase    PASSWORD :'liquibase_pw';
ALTER ROLE bikerental_app          PASSWORD :'app_pw';
ALTER ROLE bikerental_app_migrator PASSWORD :'migrator_pw';
EOSQL
