-- Privilege model for the bikerental database. Runs as the postgres superuser
-- against POSTGRES_DB on first cluster init. Each role gets the minimum it needs.

-- Admin owns the database and the public schema.
ALTER DATABASE bikerental OWNER TO bikerental_admin;
ALTER SCHEMA public OWNER TO bikerental_admin;

-- Start from a closed schema, then grant deliberately.
REVOKE ALL ON SCHEMA public FROM PUBLIC;

GRANT CONNECT ON DATABASE bikerental
    TO bikerental_admin, bikerental_liquibase, bikerental_app, bikerental_app_migrator;

-- DDL roles may create objects in public.
GRANT USAGE, CREATE ON SCHEMA public
    TO bikerental_admin, bikerental_liquibase, bikerental_app_migrator;

-- Runtime role: schema usage only. DML comes from default privileges below.
GRANT USAGE ON SCHEMA public TO bikerental_app;

-- Objects created by the migration roles are automatically DML-accessible to
-- the app role and fully owned-equivalent to admin. Covers the future split of
-- app_migrator into liquibase (creates) + app (reads/writes).
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_liquibase IN SCHEMA public
    GRANT ALL ON TABLES TO bikerental_admin;

ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO bikerental_app;
ALTER DEFAULT PRIVILEGES FOR ROLE bikerental_app_migrator IN SCHEMA public
    GRANT ALL ON TABLES TO bikerental_admin;
