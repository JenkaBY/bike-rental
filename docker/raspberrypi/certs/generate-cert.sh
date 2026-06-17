#!/usr/bin/env bash
set -Eeuo pipefail

# Generates a self-signed TLS certificate/key for PostgreSQL using Docker, so
# no host-side OpenSSL installation is required (works on Linux and Windows).
# Uses the same postgres:15-alpine image already pulled for the database.
#
# Run once before the first `docker compose up`. Output is written to this
# folder and is gitignored.
#
# Usage: ./generate-cert.sh [CN]
#   CN defaults to 'bikerental-pi'; for Render to connect, pass your KeenDNS
#   hostname (e.g. ./generate-cert.sh yourname.keenetic.link). The CN does not
#   need to be verifiable when clients use sslmode=require.

CN="${1:-bikerental-pi}"
OUT_DIR="$(cd "$(dirname "$0")" && pwd)"

docker run --rm \
    -v "${OUT_DIR}:/out" \
    postgres:15-alpine \
    openssl req -new -x509 -days 825 -noenc \
        -newkey rsa:2048 \
        -keyout /out/server.key \
        -out /out/server.crt \
        -subj "/CN=${CN}"

# Permissions are also enforced by entrypoint-wrapper.sh inside the container,
# but set them here too for clarity. chmod is a no-op on Windows NTFS.
chmod 600 "${OUT_DIR}/server.key"
chmod 644 "${OUT_DIR}/server.crt"

echo "Generated server.crt and server.key in ${OUT_DIR} (CN=${CN})."
