#!/usr/bin/env bash
set -Eeuo pipefail

# Runs as root before the official entrypoint drops privileges to the postgres
# user. Prepares host-mounted material that PostgreSQL needs with strict
# ownership/permissions, then hands off to the stock docker-entrypoint.

# 1) Render pg_hba.conf from the template, substituting the operator's LAN subnet.
if [ -f /etc/postgresql/pg_hba.conf.template ]; then
    sed "s#__LAN_SUBNET__#${LAN_SUBNET:-192.168.1.0/24}#g" \
        /etc/postgresql/pg_hba.conf.template > /etc/postgresql/pg_hba.conf
    chown postgres:postgres /etc/postgresql/pg_hba.conf
    chmod 600 /etc/postgresql/pg_hba.conf
fi

# 2) Stage TLS material. The key must be 0600 and owned by postgres, which a
#    read-only host bind-mount cannot guarantee, so copy it into place here.
if [ -f /tls/server.key ]; then
    mkdir -p /etc/postgresql/certs
    cp /tls/server.crt /etc/postgresql/certs/server.crt
    cp /tls/server.key /etc/postgresql/certs/server.key
    chown -R postgres:postgres /etc/postgresql/certs
    chmod 644 /etc/postgresql/certs/server.crt
    chmod 600 /etc/postgresql/certs/server.key
else
    echo "WARNING: /tls/server.key not found - run certs/generate-cert.sh before starting." >&2
fi

# 3) Ensure the log directory is writable by postgres (log collector / fail2ban).
mkdir -p /var/log/postgresql
chown postgres:postgres /var/log/postgresql

exec docker-entrypoint.sh "$@"
