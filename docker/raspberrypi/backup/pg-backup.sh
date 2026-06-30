#!/usr/bin/env bash
set -Eeuo pipefail

# Daily maintenance: dumps the database to a timestamped gzip file, prunes old
# dumps, and prunes old PostgreSQL log files. Connects through the container's
# local socket (trusted) as the postgres superuser, so no password is needed.
# Driven by the systemd timer on the Pi, or run manually.
#
# Configurable via environment (the systemd unit loads .env):
#   PG_CONTAINER        container name             (default: bike-rental-postgres)
#   POSTGRES_DB         database to dump           (default: bikerental)
#   BACKUP_DIR          host directory for dumps   (default: ./backups)
#   RETENTION_DAYS      delete dumps older than N  (default: 14)
#   LOG_RETENTION_DAYS  delete logs older than N   (default: 7)

CONTAINER="${PG_CONTAINER:-bike-rental-postgres}"
DB="${POSTGRES_DB:-bikerental}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
LOG_RETENTION_DAYS="${LOG_RETENTION_DAYS:-7}"

mkdir -p "$BACKUP_DIR"
TS="$(date -u +%Y%m%dT%H%M%SZ)"
FILE="${BACKUP_DIR}/${DB}_${TS}.sql.gz"

docker exec "$CONTAINER" pg_dump -U postgres -d "$DB" --clean --if-exists \
    | gzip > "$FILE"
echo "Backup written: ${FILE}"

find "$BACKUP_DIR" -name "${DB}_*.sql.gz" -type f -mtime "+${RETENTION_DAYS}" -delete
echo "Pruned dumps older than ${RETENTION_DAYS} days."

# Log retention. The log files are owned by the container's postgres user, so
# delete them from inside the container (root there) rather than on the host.
docker exec "$CONTAINER" find /var/log/postgresql -name 'postgresql-*.log' \
    -type f -mtime "+${LOG_RETENTION_DAYS}" -delete
echo "Pruned logs older than ${LOG_RETENTION_DAYS} days."
