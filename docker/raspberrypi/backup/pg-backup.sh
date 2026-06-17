#!/usr/bin/env bash
set -Eeuo pipefail

# Dumps the database to a timestamped gzip file and prunes old dumps. Connects
# through the container's local socket (trusted) as the postgres superuser, so
# no password is needed. Driven by the systemd timer on the Pi, or run manually.
#
# Configurable via environment (the systemd unit loads .env):
#   PG_CONTAINER    container name              (default: bike-rental-postgres)
#   POSTGRES_DB     database to dump            (default: bikerental)
#   BACKUP_DIR      host directory for dumps    (default: ./backups)
#   RETENTION_DAYS  delete dumps older than N   (default: 14)

CONTAINER="${PG_CONTAINER:-bike-rental-postgres}"
DB="${POSTGRES_DB:-bikerental}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

mkdir -p "$BACKUP_DIR"
TS="$(date -u +%Y%m%dT%H%M%SZ)"
FILE="${BACKUP_DIR}/${DB}_${TS}.sql.gz"

docker exec "$CONTAINER" pg_dump -U postgres -d "$DB" --clean --if-exists \
    | gzip > "$FILE"
echo "Backup written: ${FILE}"

find "$BACKUP_DIR" -name "${DB}_*.sql.gz" -type f -mtime "+${RETENTION_DAYS}" -delete
echo "Pruned dumps older than ${RETENTION_DAYS} days."
