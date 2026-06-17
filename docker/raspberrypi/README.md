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
  entrypoint-wrapper.sh      Renders pg_hba, fixes TLS key perms, log dir
  postgresql.conf           TLS, scram, Pi-sized tuning, logging
  pg_hba.conf.template       Per-role access rules (__LAN_SUBNET__ substituted)
  init/01-roles.sh           Creates the 4 login roles, sets passwords
  init/02-grants.sql         Ownership, grants, default privileges
certs/generate-cert.sh      One-off self-signed cert generator
backup/pg-backup.sh         pg_dump -> timestamped gzip, prunes old dumps
systemd/                    pg-backup.service + .timer (Pi only)
security/fail2ban/          Filter + jail to ban brute-forcers
```

---

## Database roles

| Role                      | Purpose                                                                   | Privileges                                | Reachable from         |
|---------------------------|---------------------------------------------------------------------------|-------------------------------------------|------------------------|
| `bikerental_admin`        | Owner/admin (you)                                                         | DB + schema owner, `ALL`, `CREATEROLE`    | LAN only               |
| `bikerental_liquibase`    | Future CI/CD migrations                                                   | `CONNECT` + `CREATE` on schema (DDL)      | LAN only               |
| `bikerental_app`          | Future backend runtime                                                    | DML only (no DDL), via default privileges | LAN only               |
| `bikerental_app_migrator` | **Used now** by the Render app (Liquibase at startup **and** runtime DML) | DDL + DML                                 | Internet (`0.0.0.0/0`) |

The superuser `postgres` is reachable only over the container's local socket
(never over TCP). When CI/CD is added, split `app_migrator` into
`liquibase` (runs migrations) + `app` (runtime) — both already have the right
default privileges.

---

## Prerequisites

1. **Public IP, not CGNAT.** Compare your Keenetic WAN IP (router UI) with
   `https://ifconfig.me` from a LAN machine. If they differ you are behind CGNAT
   and inbound port-forwarding cannot work — stop here.
2. **Docker Engine + Compose plugin** on the Pi (arm64):
   ```bash
   curl -fsSL https://get.docker.com | sh
   sudo usermod -aG docker "$USER"   # re-login afterwards
   ```

---

## Setup (on the Pi)

```bash
# 1. Clone the repo into your home directory (no sudo needed)
git clone <your-repo-url> ~/bike-rental
cd ~/bike-rental/docker/raspberrypi

# 2. Configure secrets
cp .env.example .env
# edit .env: set strong passwords (openssl rand -base64 24), LAN_SUBNET, port,
# and replace YOUR_USERNAME in BACKUP_DIR with your actual Pi username.

# 3. Generate the TLS certificate (CN = the hostname clients will use)
chmod +x ./certs/generate-cert.sh
./certs/generate-cert.sh yourname.keenetic.link

# 4. Build and start
docker compose up -d --build
docker compose ps          # postgres should become healthy
docker compose logs -f postgres
```

### Preparing the data directory and volume on the NVMe

The `pgdata` volume is marked `external` in the Compose file — Compose never
creates or deletes it automatically. You create it once with an explicit host
path binding:

```bash
# 1. Create the host directory and give ownership to the container's postgres user (uid 70)
sudo mkdir -p /data/postgres && sudo chown 70:70 /data/postgres

# 2. Create the named volume backed by that directory
docker volume create \
  --driver local \
  --opt type=none \
  --opt o=bind \
  --opt device=/data/postgres \
  bike-rental-pgdata
```

After this, `docker compose up -d` attaches to the existing volume.
`docker compose down` (without `-v`) leaves it untouched.
`docker volume inspect bike-rental-pgdata` shows the host path.

**On Windows** create a plain named volume instead (no host path binding):
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
# edit the jail's logpath if you cloned somewhere other than /opt/bike-rental
sudo systemctl restart fail2ban
sudo fail2ban-client status postgresql
```

---

## Backups (do not skip)

```bash
# Install the daily timer. The unit uses %i (the instance name) as your username,
# so enable it as pg-backup@<your-username> to resolve the home directory correctly.
sudo cp systemd/pg-backup.service /etc/systemd/system/pg-backup@.service
sudo cp systemd/pg-backup.timer   /etc/systemd/system/pg-backup@.timer
sudo systemctl daemon-reload
sudo systemctl enable --now pg-backup@${USER}.timer
systemctl list-timers pg-backup@${USER}.timer

# Run once on demand
./backup/pg-backup.sh
```

Dumps land in `BACKUP_DIR` (default `./backups`). **Copy them off-box
periodically** (another machine / cloud) — a Pi failure should not lose data.

Restore example:

```bash
gunzip -c backups/bikerental_<timestamp>.sql.gz \
  | docker exec -i bike-rental-postgres psql -U postgres -d bikerental
```

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
