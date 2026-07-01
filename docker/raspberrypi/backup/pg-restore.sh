#!/usr/bin/env bash
set -Eeuo pipefail

# Restores a gzipped SQL dump into a BRAND NEW database named
# bikerental_restore_<backup_timestamp>, then grants the runtime role
# (bikerental_app) DML access to the restored tables. The live database is never
# touched, so this is safe to run at any time to verify or inspect a backup.
#
# Usage:
#   ./pg-restore.sh [BACKUP_FILE]
#   ./pg-restore.sh --env-file /path/to/.env [BACKUP_FILE]
#
#   BACKUP_FILE   Path to a *.sql.gz dump. If omitted, the newest dump matching
#                 "<POSTGRES_DB>_*.sql.gz" in BACKUP_DIR is used.
#
# The dump is loaded through the container's local socket as the postgres
# superuser (trusted), so no password is needed.
#
# Configurable via environment (an .env file is sourced automatically - see
# ENV_FILE below; variable names match docker/raspberrypi/.env.example):
#   PG_CONTAINER   container name                    (default: bike-rental-postgres)
#   POSTGRES_DB    source database name / dump prefix (default: bikerental)
#   BACKUP_DIR     host directory holding the dumps   (default: ./backups)
#   APP_ROLE       runtime role granted DML access    (default: bikerental_app)
#   ADMIN_ROLE     owner of the restored database     (default: bikerental_admin)
#   ENV_FILE       path to the .env to source         (default: ../.env next to this script)

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

ENV_FILE="${ENV_FILE:-${SCRIPT_DIR}/../.env}"
BACKUP_FILE=""

while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help)
            sed -n '3,30p' "${BASH_SOURCE[0]}" | sed 's/^# \{0,1\}//'
            exit 0
            ;;
        --env-file)
            ENV_FILE="$2"
            shift 2
            ;;
        --env-file=*)
            ENV_FILE="${1#*=}"
            shift
            ;;
        -*)
            echo "Unknown option: $1" >&2
            exit 2
            ;;
        *)
            BACKUP_FILE="$1"
            shift
            ;;
    esac
done

# Load .env so BACKUP_DIR / POSTGRES_DB / etc. match the running stack.
if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    set +a
    echo "Loaded environment from ${ENV_FILE}"
else
    echo "No env file at ${ENV_FILE}; relying on current environment and defaults."
fi

CONTAINER="${PG_CONTAINER:-bike-rental-postgres}"
DB="${POSTGRES_DB:-bikerental}"
BACKUP_DIR="${BACKUP_DIR:-${SCRIPT_DIR}/../backups}"
APP_ROLE="${APP_ROLE:-bikerental_app}"
ADMIN_ROLE="${ADMIN_ROLE:-bikerental_admin}"

# Resolve the backup file: explicit argument wins, otherwise pick the newest
# dump for this database in BACKUP_DIR.
if [ -z "$BACKUP_FILE" ]; then
    BACKUP_FILE="$(find "$BACKUP_DIR" -maxdepth 1 -type f -name "${DB}_*.sql.gz" \
        -printf '%T@ %p\n' 2>/dev/null | sort -n | tail -1 | cut -d' ' -f2-)"
    if [ -z "$BACKUP_FILE" ]; then
        echo "No backups matching ${DB}_*.sql.gz found in ${BACKUP_DIR}." >&2
        exit 1
    fi
    echo "No backup file given; using latest: ${BACKUP_FILE}"
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Backup file not found: ${BACKUP_FILE}" >&2
    exit 1
fi

# Verify the archive before touching the database.
gunzip -t "$BACKUP_FILE"
echo "Archive integrity OK: ${BACKUP_FILE}"

# Derive the timestamp from the filename (backups are named
# "<db>_YYYYMMDDTHHMMSSZ.sql.gz"). Fall back to now if the pattern is absent.
BASENAME="$(basename "$BACKUP_FILE")"
if [[ "$BASENAME" =~ ([0-9]{8}T[0-9]{6}Z) ]]; then
    BACKUP_TS="${BASH_REMATCH[1]}"
else
    BACKUP_TS="$(date -u +%Y%m%dT%H%M%SZ)"
    echo "Filename carries no timestamp; using current time ${BACKUP_TS}."
fi

# Postgres identifiers are simpler unquoted when lowercase, so fold the T/Z.
TARGET_DB="bikerental_restore_$(echo "$BACKUP_TS" | tr 'A-Z' 'a-z')"

# Fail fast if the container is not up.
if ! docker exec "$CONTAINER" pg_isready -U postgres >/dev/null 2>&1; then
    echo "Container ${CONTAINER} is not accepting connections." >&2
    exit 1
fi

# Refuse to clobber an existing restore database - restores are immutable.
exists="$(docker exec "$CONTAINER" psql -tAc \
    "SELECT 1 FROM pg_database WHERE datname = '${TARGET_DB}'" -U postgres)"
if [ "$exists" = "1" ]; then
    echo "Database ${TARGET_DB} already exists. Drop it first:" >&2
    echo "  docker exec ${CONTAINER} dropdb -U postgres ${TARGET_DB}" >&2
    exit 1
fi

echo "Creating database ${TARGET_DB} (owner ${ADMIN_ROLE})."
docker exec "$CONTAINER" createdb -U postgres -O "$ADMIN_ROLE" "$TARGET_DB"

echo "Restoring ${BACKUP_FILE} into ${TARGET_DB}..."
gunzip -c "$BACKUP_FILE" \
    | docker exec -i "$CONTAINER" psql -v ON_ERROR_STOP=1 -U postgres -d "$TARGET_DB"

# Grant the runtime role DML access to everything just restored, plus anything
# created later in the schema. Objects come in owned by whoever owned them in the
# source dump, so default privileges do not cover them - grant explicitly.
echo "Granting ${APP_ROLE} DML access on ${TARGET_DB}."
docker exec -i "$CONTAINER" psql -v ON_ERROR_STOP=1 -U postgres -d "$TARGET_DB" \
    -v app_role="$APP_ROLE" -v dbname="$TARGET_DB" <<'EOSQL'
GRANT CONNECT ON DATABASE :"dbname" TO :"app_role";
GRANT USAGE ON SCHEMA public TO :"app_role";
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO :"app_role";
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO :"app_role";
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO :"app_role";
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO :"app_role";
EOSQL

TABLE_COUNT="$(docker exec "$CONTAINER" psql -tAc \
    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'" \
    -U postgres -d "$TARGET_DB")"

echo
echo "Restore complete."
echo "  Database : ${TARGET_DB}"
echo "  Tables   : ${TABLE_COUNT} in schema public"
echo "  Access   : ${APP_ROLE} (SELECT/INSERT/UPDATE/DELETE), owner ${ADMIN_ROLE}"
echo
echo "Inspect it:"
echo "  docker exec ${CONTAINER} psql -U postgres -d ${TARGET_DB} -c '\\dt'"
echo "Drop it when done:"
echo "  docker exec ${CONTAINER} dropdb -U postgres ${TARGET_DB}"
