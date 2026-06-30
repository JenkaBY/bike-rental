# PostgreSQL on Raspberry Pi 5 (self-hosted dev database)

A persistent, version-controlled PostgreSQL 15 deployment for a Raspberry Pi 5
(Ubuntu 26.04, NVMe). Replaces the expiring free-tier database on Render.com:
the Render backend connects to this DB over the internet, while **only the
PostgreSQL port is exposed** — SSH and everything else stay LAN-only.

This is **stage 1 (PostgreSQL only)**. The same Compose file is structured so the
backend and frontend can be added later.

> ⚠️ **Security reality.** An internet-reachable PostgreSQL is scanned and
> brute-forced continuously. Keep only **non-sensitive dev data** here and rely
> on the backups below. Hardening in place: TLS required, `scram-sha-256`,
> least-privilege roles, firewall, and fail2ban.

---

## Contents

```
docker-compose.yml          Compose stack (single postgres service for now)
.env.example                Copy to .env and fill in secrets (gitignored)
postgres/
  Dockerfile                Thin image: adds an entrypoint wrapper
  entrypoint-wrapper.sh      Renders pg_hba (LAN subnet, router IP, app DB list), TLS, log dir
  postgresql.conf           TLS, scram, Pi-sized tuning, logging
  pg_hba.conf.template       Per-role rules (__LAN_SUBNET__, __ROUTER_IP__, __APP_DATABASES__ substituted)
  init/01-roles.sh           Creates the 4 login roles, sets passwords
  init/02-provision-databases.sh  Creates every app DB + applies the privilege model
certs/generate-cert.sh      One-off self-signed cert generator
backup/pg-backup.sh         pg_dump -> timestamped gzip, prunes old dumps
systemd/                    pg-backup.service + .timer + install-backup-timer.sh (Pi only)
security/fail2ban/          Filter + jail to ban brute-forcers
TESTING.md                  Step-by-step verification guide (run after setup/changes)
```

After setup, work through [TESTING.md](TESTING.md) to verify role privileges,
network access restrictions, storage location, logs, and backups.

---

## Database roles

| Role                      | Purpose                                                                   | Privileges                                | Reachable from         |
|---------------------------|---------------------------------------------------------------------------|-------------------------------------------|------------------------|
| `bikerental_admin`        | Owner/admin (you)                                                         | DB + schema owner, `ALL`, `CREATEROLE`    | LAN only               |
| `bikerental_liquibase`    | CI/CD migrations                                                          | `CONNECT` + `CREATE` on schema (DDL)      | Internet (`0.0.0.0/0`) |
| `bikerental_app`          | Backend runtime                                                           | DML only (no DDL), via default privileges | Internet (`0.0.0.0/0`) |
| `bikerental_app_migrator` | **Used now** by the Render app (Liquibase at startup **and** runtime DML) | DDL + DML                                 | Internet (`0.0.0.0/0`) |

Roles are **cluster-wide** — the same four roles apply to every database in the
list (see below). The superuser `postgres` is reachable only over the
container's local socket (never over TCP). When CI/CD is added, split
`app_migrator` into `liquibase` (runs migrations) + `app` (runtime) — both
already have the right default privileges.

> ⚠️ Three roles are internet-reachable, so each is a brute-force target. Give
> **all of them** strong, unique passwords. `bikerental_liquibase` can run DDL,
> so guard its password especially well.

---

## Databases (dev + remote test)

One PostgreSQL instance hosts multiple databases. `POSTGRES_DB` (default
`bikerental`) is the primary; `EXTRA_DATABASES` in `.env` is a space-separated
list of additional databases created with the **same roles and privileges** and
reachable over the internet for the `liquibase`/`app`/`app_migrator` roles.

```bash
# .env
POSTGRES_DB=bikerental
EXTRA_DATABASES=bikerental_test          # add more, space-separated
```

`pg_hba.conf` automatically allows the internet-facing roles on every database
in `POSTGRES_DB + EXTRA_DATABASES` (the entrypoint substitutes the list into
`__APP_DATABASES__`). To add another database later, just append it to
`EXTRA_DATABASES`, then create it on the existing cluster (see below) and
restart — no `pg_hba` edits needed.

### Adding a database to an already-running cluster

Init scripts only run on a **fresh** cluster, so for an existing one create the
database and apply the privilege model manually (this is exactly what
`init/02-provision-databases.sh` does on first init):

```bash
# 1. Add the name to EXTRA_DATABASES in .env, then re-render pg_hba + reload:
docker compose up -d
docker exec bike-rental-postgres psql -U postgres -c "SELECT pg_reload_conf();"

# 2. Create the database and apply grants (run from the repo folder on the Pi):
docker exec -e POSTGRES_USER=postgres -e POSTGRES_DB=bikerental \
  -e EXTRA_DATABASES=bikerental_test bike-rental-postgres \
  bash /docker-entrypoint-initdb.d/02-provision-databases.sh
```

The script is idempotent: it skips databases that already exist and re-applies
grants safely.

---

## Prerequisites

1. **Public IP, not CGNAT.** Compare your Keenetic WAN IP (router UI) with
   `https://ifconfig.me` from a LAN machine. If they differ you are behind CGNAT
   and inbound port-forwarding cannot work — stop here.
2. **Docker Engine + Compose plugin** on the Pi (arm64) — use the **official**
   package, NOT the Ubuntu snap:
   ```bash
   curl -fsSL https://get.docker.com | sh
   sudo usermod -aG docker "$USER"   # re-login afterwards
   ```
   > ⚠️ **Do not use snap Docker** (`snap install docker`). The snap runs
   > confined and **cannot bind-mount** host paths like `/data/postgres`, so the
   > external data volume fails with `failed to mount local volume ... no such
   > file or directory`. Check with `docker info | grep "Docker Root Dir"` — if
   > it shows `/var/snap/docker/...`, remove it first: `sudo snap remove docker`,
   > then install via the script above.

---

## Setup (on the Pi)

```bash
# 1. Clone the repo into your home directory (no sudo needed)
git clone <your-repo-url> ~/bike-rental
cd ~/bike-rental/docker/raspberrypi

# 2. Configure secrets
cp .env.example .env
# edit .env: set strong passwords (openssl rand -base64 24), LAN_SUBNET,
# ROUTER_IP, PG_PUBLISHED_PORT, EXTRA_DATABASES, and replace YOUR_USERNAME in
# BACKUP_DIR with your actual Pi username.

# 3. Generate the TLS certificate (CN = the hostname clients will use)
./certs/generate-cert.sh yourname.keenetic.link

# 4. Create the data directory and the external volume — REQUIRED before the
#    first start. The pgdata volume is `external`, so Compose will NOT create it;
#    `docker compose up` fails with "external volume not found" if you skip this.
sudo mkdir -p /data/postgres && sudo chown 70:70 /data/postgres   # uid 70 = postgres in the container
docker volume create \
  --driver local --opt type=none --opt o=bind --opt device=/data/postgres \
  bike-rental-pgdata

# 5. Build and start
docker compose up -d --build
docker compose ps          # postgres should become healthy
docker compose logs -f postgres
```

### About the data volume

The `pgdata` volume is marked `external`: you create it once (step 4) and
Compose only ever attaches to it. This makes the data lifecycle explicit —
`docker compose down` (without `-v`) leaves it untouched, and no Compose command
can wipe it; you would have to run `docker volume rm` yourself.

- Inspect the host path: `docker volume inspect bike-rental-pgdata`
- The `device` path should match `PGDATA_HOST_PATH` in `.env` (kept as the
  documented source of truth for where the data lives).

**On Windows** create a plain named volume instead of the bind-backed one in
step 4 — never bind-mount PostgreSQL data to a Windows host folder:
```bash
docker volume create bike-rental-pgdata
```

---

## Exposing only the database (Keenetic + firewall)

1. **Keenetic port forward:** forward an external port (e.g. `54321`) → the Pi's
   LAN IP, internal port = `PG_PUBLISHED_PORT` (5432). Forward **nothing else** —
   no SSH.
2. **KeenDNS:** enable **direct mode** to get a stable hostname
   (`yourname.keenetic.link`) that tracks your changing home IP.
3. **Firewall on the Pi:**
   ```bash
   sudo apt install -y ufw
   sudo ufw default deny incoming
   sudo ufw default allow outgoing
   sudo ufw allow 5432/tcp                       # database (forwarded port lands here)
   sudo ufw allow from 192.168.1.0/24 to any port 22 proto tcp   # SSH: LAN only
   sudo ufw enable
   sudo ufw status verbose
   ```
4. **OS auto-updates:**
   ```bash
   sudo apt install -y unattended-upgrades
   sudo dpkg-reconfigure -plow unattended-upgrades
   ```

### fail2ban (ban brute-forcers)

```bash
sudo apt install -y fail2ban
sudo cp security/fail2ban/filter.d/postgresql.conf /etc/fail2ban/filter.d/
sudo cp security/fail2ban/jail.d/postgresql.conf   /etc/fail2ban/jail.d/
# edit the jail's logpath if you cloned somewhere other than ~/bike-rental
sudo systemctl restart fail2ban
sudo fail2ban-client status postgresql
```

---

## Backups (do not skip)

```bash
# Install the daily timer. The script auto-detects your username, substitutes it
# into the unit, installs it, and enables the timer.
./systemd/install-backup-timer.sh

# Run once on demand
./backup/pg-backup.sh
```

The backup service runs as **your user** (not root), so dumps are owned by you
and readable without `sudo`. Re-run `install-backup-timer.sh` if you change the
user or the schedule in `pg-backup.timer`.

> ⚠️ **`BACKUP_DIR` must be owned by your user.** The timer runs `pg-backup.sh`
> as your user, so it cannot write into a directory owned by root. If you ever
> ran the script with `sudo`, the folder became root-owned and backups fail with
> `Permission denied`. Fix the ownership once (adjust the path to your
> `BACKUP_DIR`):
> ```bash
> sudo chown -R {USER}:{USER} /home/{USER}/backups
> ```
> Never run `./backup/pg-backup.sh` with `sudo` — it doesn't need root.

Dumps land in `BACKUP_DIR` (set to an absolute path in `.env`). **Copy them
off-box periodically** (another machine / cloud) — a Pi failure should not lose
data.

> The backup script currently dumps only the primary database (`POSTGRES_DB`,
> i.e. `bikerental`). The `bikerental_test` database is **not** included.

---

## Restoring from a backup

Dumps are **plain SQL, gzipped**, created with `pg_dump --clean --if-exists` —
each dump DROPs the existing objects and recreates them. Restore with `psql`
(not `pg_restore`, which is only for custom/directory-format dumps). Run these on
the Pi from `~/bike-rental/docker/raspberrypi`.

### 1. Find and inspect a backup

```bash
BACKUP_DIR=$(grep -E '^BACKUP_DIR=' .env | cut -d= -f2)
ls -lh "$BACKUP_DIR"                              # list dumps, newest last

BACKUP="$BACKUP_DIR/bikerental_<timestamp>.sql.gz"   # pick one
gunzip -t "$BACKUP" && echo "archive OK"          # verify integrity
gunzip -c "$BACKUP" | grep -c '^CREATE TABLE'     # sanity: how many tables
gunzip -c "$BACKUP" | less                         # browse the SQL (q to quit)
```

### 2A. Restore into a NEW database first (recommended — verify before switching)

Safest path: restore into a scratch database, check it, then decide. This never
touches your live data.

```bash
# Create an empty target owned by admin
docker exec bike-rental-postgres createdb -U postgres -O bikerental_admin bikerental_restore

# Load the dump (stops on first error)
gunzip -c "$BACKUP" \
  | docker exec -i bike-rental-postgres psql -v ON_ERROR_STOP=1 -U postgres -d bikerental_restore

# Verify
docker exec bike-rental-postgres psql -U postgres -d bikerental_restore -c "\dt"
docker exec bike-rental-postgres psql -U postgres -d bikerental_restore \
  -c "SELECT count(*) FROM customers;"     # spot-check a known table
```

When satisfied, either point your app at `bikerental_restore`, or drop the live
DB and rename (`bikerental` → keep a copy, rename restore in). To clean up the
scratch DB: `docker exec bike-rental-postgres dropdb -U postgres bikerental_restore`.

### 2B. Restore in place (overwrite the live `bikerental`)

⚠️ This replaces the current contents (the dump's `--clean` drops objects first).

```bash
# 1. Pause writers: stop the Render service / app so nothing writes mid-restore.
# 2. Take a safety dump of the current state first:
./backup/pg-backup.sh

# 3. Restore
gunzip -c "$BACKUP" \
  | docker exec -i bike-rental-postgres psql -v ON_ERROR_STOP=1 -U postgres -d bikerental
```

Roles referenced by the dump (`bikerental_admin`, …) already exist cluster-wide,
so the ownership statements in the dump apply cleanly. `-v ON_ERROR_STOP=1`
aborts on the first error instead of continuing with a half-applied restore.

### 3. Extract a single table from a dump

Restore the whole dump into a scratch DB (step 2A), then copy just that table:

```bash
docker exec bike-rental-postgres pg_dump -U postgres -d bikerental_restore \
  -t public.my_table --data-only \
  | docker exec -i bike-rental-postgres psql -U postgres -d bikerental
```

### 4. Disaster recovery (the Pi died — restore on a fresh machine)

1. Stand up the stack on the new Pi (Setup steps 1–5). The init scripts create
   the empty databases **and the four roles** on the fresh cluster.
2. Copy your off-box `*.sql.gz` onto the new Pi.
3. Restore in place as in **2B** (no need to pause anything — it's a new box).

This is why off-box copies matter: a dump that only lives on the dead Pi is
worthless.

### 5. Restoring from a remote machine

You can also restore over the network from your laptop using an internet-facing
role with DDL rights (`bikerental_app_migrator`):

```bash
gunzip -c bikerental_<timestamp>.sql.gz \
  | psql "host=yourname.keenetic.pro port=6543 dbname=bikerental_restore \
          user=bikerental_app_migrator sslmode=require" -v ON_ERROR_STOP=1
```

The local-socket superuser method (on the Pi) is preferred — it avoids sending
the whole dump over the wire and sidesteps per-object ownership permission edge
cases.

---

## Connecting the Render backend

Set these as Render environment variables (not committed):

```
DATASOURCE_URL=jdbc:postgresql://yourname.keenetic.link:54321/bikerental?sslmode=require
DATASOURCE_USER=bikerental_app_migrator
DATASOURCE_SECRET=<BIKERENTAL_APP_MIGRATOR_PASSWORD>
```

`bikerental_app_migrator` both runs Liquibase at startup and serves runtime
queries. Redeploy on Render; the app should start, apply migrations, and serve.

### Connecting to the remote test database

The test database is the same host/port, only the database name changes:

```
jdbc:postgresql://yourname.keenetic.pro:54321/bikerental_test?sslmode=require
```

Any of the internet-facing roles work (`bikerental_app_migrator` for full
DDL+DML, `bikerental_app` for runtime-only, `bikerental_liquibase` for
migrations). psql from a remote machine:

```bash
psql "host=yourname.keenetic.pro port=54321 dbname=bikerental_test \
  user=bikerental_app_migrator sslmode=require"
```

---

## Running on Windows (local development)

The same Compose file works on Windows (Docker Desktop) with these notes:

1. **Keep `PGDATA_VOLUME` a named volume** (the default). Never bind-mount the
   data directory to a Windows host folder — PostgreSQL fails on it (permissions
   / fsync). Named volumes are stored inside the Docker VM and are safe.
2. **Generate the cert** using `certs/generate-cert.sh` from Git Bash — it uses
   Docker internally, so no host-side OpenSSL is needed. Works identically on
   Linux and Windows.
3. **Backups:** the systemd timer is Linux-only. On Windows run `pg-backup.sh`
   from Git Bash, or schedule it with Task Scheduler.
4. **`ufw` / `fail2ban` / `unattended-upgrades`** are Linux/Pi-only and not
   needed for local dev (don't forward any ports from your home router to a dev
   laptop).
5. Scripts are forced to LF via `.gitattributes`, so they run correctly when
   mounted into the Linux container.

Bring it up the same way: `docker compose up -d --build`.

---

## Why Docker Compose (not Ansible/Kubernetes)?

Docker and Compose are production-grade for a **single host**, which is exactly
this case; they are widely used this way for small services and homelabs.
Multi-node / high-availability would call for Kubernetes (k3s), Swarm, or Nomad —
overkill for one Pi. A native `apt install postgresql` was rejected because its
config drifts off the machine and out of version control.

---

## Verify

```bash
# Healthy + accepting connections
docker compose ps
docker exec bike-rental-postgres pg_isready -U postgres -d bikerental

# TLS is enforced (run from a LAN machine that has psql):
psql "host=<pi-lan-ip> port=5432 user=bikerental_admin dbname=bikerental sslmode=disable"  # must FAIL
psql "host=<pi-lan-ip> port=5432 user=bikerental_admin dbname=bikerental sslmode=require"  # must SUCCEED

# Role boundaries (as bikerental_app, DDL must be denied):
#   CREATE TABLE t(id int);   -> ERROR: permission denied for schema public
```
