# Verification Guide

Step-by-step tests proving the deployment behaves as designed: role privileges,
network access restrictions, physical storage location, logs, and backups. Run
the whole guide after first setup and after any change to roles, `pg_hba`, the
volume, or the compose file.

## Testing strategy

Two deliberate connection paths:

| What we test | How | Why |
|---|---|---|
| **Role privileges** (Section A) | Local socket via `docker exec` (no password, `local trust`) | Privileges don't depend on connection source — isolates "what a role may do" |
| **Network access & TLS** (Section B) | TCP from a **LAN machine** and an **external machine** | `pg_hba.conf` source matching is the thing under test |

A test **passes** when the actual result matches "Expect". Errors are not
failures when the test expects them — read each "Expect" carefully.

### Prerequisites

- A `psql` client on (a) a machine **on your LAN**, and (b) a machine **off
  your LAN** (phone hotspot / mobile tether / a cloud VM). The external machine
  is mandatory — several guarantees can only be proven from the real internet.
- SSH access to the Pi for the `docker exec` and filesystem checks.

Set these once per shell (adjust to your values):

```bash
PIHOST=192.168.1.168            # the Pi's LAN IP (use the IP, NOT the hostname)
EXTHOST=yourname.keenetic.pro   # KeenDNS hostname
EXTPORT=6543                    # router's forwarded external port
TESTDB=bikerental_test          # use the test DB so dev data is untouched
```

---

## Section A — Role privileges

Goal: `app` can only do DML, `liquibase`/`app_migrator` can do DDL, `admin` is
full + can manage roles. Run these on the Pi.

### A1. `bikerental_app` — DML allowed, DDL denied

```bash
# Setup: create a table as a DDL role so default privileges grant app access
docker exec bike-rental-postgres psql -U bikerental_liquibase -d "$TESTDB" \
  -c "CREATE TABLE perm_probe (id int);"

# Expect SUCCESS — app may INSERT/SELECT/UPDATE/DELETE
docker exec bike-rental-postgres psql -U bikerental_app -d "$TESTDB" \
  -c "INSERT INTO perm_probe VALUES (1);" \
  -c "SELECT count(*) FROM perm_probe;"

# Expect FAILURE — "permission denied for schema public" (no CREATE)
docker exec bike-rental-postgres psql -U bikerental_app -d "$TESTDB" \
  -c "CREATE TABLE should_fail (id int);"

# Expect FAILURE — "must be owner of table perm_probe" (no DDL on others' tables)
docker exec bike-rental-postgres psql -U bikerental_app -d "$TESTDB" \
  -c "DROP TABLE perm_probe;"

# Expect FAILURE — app may not create roles
docker exec bike-rental-postgres psql -U bikerental_app -d "$TESTDB" \
  -c "CREATE ROLE should_fail LOGIN;"
```

### A2. `bikerental_liquibase` — DDL allowed, role management denied

```bash
# Expect SUCCESS — create table, index, alter
docker exec bike-rental-postgres psql -U bikerental_liquibase -d "$TESTDB" \
  -c "CREATE TABLE lb_probe (id int);" \
  -c "CREATE INDEX lb_probe_idx ON lb_probe(id);" \
  -c "ALTER TABLE lb_probe ADD COLUMN note text;"

# Expect FAILURE — liquibase has no CREATEROLE
docker exec bike-rental-postgres psql -U bikerental_liquibase -d "$TESTDB" \
  -c "CREATE ROLE should_fail LOGIN;"
```

### A3. `bikerental_app_migrator` — DDL + DML

```bash
# Expect SUCCESS — create AND populate (the role used by Render now)
docker exec bike-rental-postgres psql -U bikerental_app_migrator -d "$TESTDB" \
  -c "CREATE TABLE mig_probe (id int);" \
  -c "INSERT INTO mig_probe VALUES (1);"
```

### A4. `bikerental_admin` — full + role management

```bash
# Expect SUCCESS — admin owns the DB and can manage roles (CREATEROLE)
docker exec bike-rental-postgres psql -U bikerental_admin -d "$TESTDB" \
  -c "CREATE ROLE probe_role NOLOGIN;" \
  -c "DROP ROLE probe_role;" \
  -c "CREATE TABLE admin_probe (id int);"
```

### A5. Confirm the app role inherits DML on admin/migrator-created tables

```bash
# admin created admin_probe in A4; app should still be able to write to it
# Expect SUCCESS
docker exec bike-rental-postgres psql -U bikerental_app -d "$TESTDB" \
  -c "INSERT INTO admin_probe VALUES (1);"
```

### A6. Cleanup

```bash
docker exec bike-rental-postgres psql -U bikerental_admin -d "$TESTDB" \
  -c "DROP TABLE IF EXISTS perm_probe, lb_probe, mig_probe, admin_probe;"
```

---

## Section B — Network access & TLS

Goal: `admin` only from LAN; `app`/`liquibase`/`app_migrator` from the internet;
TLS always required; superuser never over TCP.

### B1. From a LAN machine — use the Pi's IP, not the hostname

> Connecting to the KeenDNS hostname from inside the LAN triggers **NAT
> hairpin** — the router rewrites the source to its own IP (`192.168.1.1`), so
> the connection looks like it came through the internet path. To test the
> genuine LAN path, connect to `$PIHOST:5432` directly.

```bash
# Expect SUCCESS — admin is allowed from the LAN subnet
psql "host=$PIHOST port=5432 dbname=bikerental user=bikerental_admin sslmode=require" -c "select 'lan-admin-ok';"

# Expect SUCCESS — app reaches the test DB
psql "host=$PIHOST port=5432 dbname=$TESTDB user=bikerental_app sslmode=require" -c "select 'lan-app-ok';"
```

### B2. From an external machine (phone hotspot / cloud VM) — the important ones

```bash
# Expect SUCCESS — app, liquibase, migrator are internet-reachable
psql "host=$EXTHOST port=$EXTPORT dbname=$TESTDB user=bikerental_app sslmode=require" -c "select 'net-app-ok';"
psql "host=$EXTHOST port=$EXTPORT dbname=$TESTDB user=bikerental_liquibase sslmode=require" -c "select 'net-liquibase-ok';"
psql "host=$EXTHOST port=$EXTPORT dbname=bikerental user=bikerental_app_migrator sslmode=require" -c "select 'net-migrator-ok';"

# Expect FAILURE — "no pg_hba.conf entry" — admin must NOT be reachable from the internet
psql "host=$EXTHOST port=$EXTPORT dbname=bikerental user=bikerental_admin sslmode=require" -c "select 'should-not-print';"
```

> ⚠️ **Critical NAT check.** The admin test in B2 **must fail**. `pg_hba.conf`
> has an explicit rule rejecting `bikerental_admin` from the router IP
> (`ROUTER_IP`, default `192.168.1.1`) *before* the LAN allow — because the
> router source-NATs inbound internet traffic to that address. Confirm the
> rejection, then watch the log during the external connection (Section D): if
> the logged client IP is `192.168.1.1` you are correctly protected (the reject
> rule caught it). If admin ever *succeeds* from the internet, either `ROUTER_IP`
> in `.env` doesn't match your router's actual LAN IP, or the change wasn't
> applied — re-render `pg_hba` (`docker compose up -d`) and re-test.

### B3. TLS is mandatory

```bash
# Expect FAILURE — "pg_hba.conf rejects connection ... no encryption"
# (non-SSL connections match no hostssl line, fall through to the reject rule)
psql "host=$PIHOST port=5432 dbname=$TESTDB user=bikerental_app sslmode=disable" -c "select 1;"
```

### B4. Superuser is not reachable over TCP

```bash
# Expect FAILURE from both LAN and internet — postgres has no hostssl rule
psql "host=$PIHOST port=5432 dbname=bikerental user=postgres sslmode=require" -c "select 1;"
```

### B5. Confirm the live connection is actually encrypted

```bash
psql "host=$PIHOST port=5432 dbname=$TESTDB user=bikerental_app sslmode=require" \
  -c "SELECT ssl, version, cipher FROM pg_stat_ssl WHERE pid = pg_backend_pid();"
# Expect: ssl = t, a TLS version (e.g. TLSv1.3) and a cipher name
```

### B6. Confirm passwords are stored as SCRAM (not md5/plaintext)

```bash
docker exec bike-rental-postgres psql -U postgres -c \
  "SELECT rolname, left(rolpassword,14) AS enc FROM pg_authid WHERE rolname LIKE 'bikerental%';"
# Expect every enc to read: SCRAM-SHA-256
```

---

## Section C — Physical storage location

Goal: the database files live in the specified host directory on the NVMe.

```bash
# 1. The named volume is bind-backed by /data/postgres
docker volume inspect bike-rental-pgdata --format '{{ .Options.device }}'
# Expect: /data/postgres

# 2. Real PostgreSQL data files are there
sudo ls /data/postgres
# Expect: PG_VERSION  base/  global/  pg_wal/  postgresql.conf ... etc.

# 3. That path is on the NVMe (not the SD card)
df -h /data/postgres
# Expect: the Filesystem column points at your NVMe device (e.g. /dev/nvme0n1p2)

# 4. Prove writes land there — note the size, insert data, note it grew
sudo du -sh /data/postgres
docker exec bike-rental-postgres psql -U bikerental_app_migrator -d "$TESTDB" \
  -c "CREATE TABLE size_probe AS SELECT generate_series(1,100000) AS n;"
docker exec bike-rental-postgres psql -U postgres -c "CHECKPOINT;"
sudo du -sh /data/postgres   # Expect: larger than before
docker exec bike-rental-postgres psql -U bikerental_app_migrator -d "$TESTDB" \
  -c "DROP TABLE size_probe;"
```

---

## Section D — Logs

Goal: logs are written to the bind-mounted host folder and readable by your
normal (non-root) user.

```bash
cd ~/bike-rental/docker/raspberrypi

# 1. Log files exist and are world-readable (mode 0644)
ls -l logs/
# Expect: postgresql-YYYY-MM-DD.log with permissions -rw-r--r--

# 2. Readable WITHOUT sudo (proves log_file_mode lets the operator read them)
tail -n 5 logs/postgresql-$(date -u +%Y-%m-%d).log
# Expect: log lines print, no "Permission denied"

# 3. Live capture during a connection — run the tail, then connect from B2,
#    and confirm the logged client IP (see the NAT check in B2)
tail -f logs/postgresql-$(date -u +%Y-%m-%d).log
# In another shell, connect externally; Expect a "connection received: host=<IP>"
# line. Confirm <IP> for a genuine external client is your real internet IP.
```

---

## Section E — Backups (and restore)

Goal: dumps are created, owned/readable by your user, valid, and restorable.

```bash
cd ~/bike-rental/docker/raspberrypi

# 1. Run a backup on demand
./backup/pg-backup.sh
# Expect: "Backup written: .../bikerental_<timestamp>.sql.gz"

# 2. The dump file is owned by your user and readable without sudo
ls -l "$(grep -E '^BACKUP_DIR=' .env | cut -d= -f2)"/bikerental_*.sql.gz | tail -1
# Expect: owner is your username, not root

# 3. The gzip is valid and contains SQL
LATEST=$(ls -t "$(grep -E '^BACKUP_DIR=' .env | cut -d= -f2)"/bikerental_*.sql.gz | head -1)
gunzip -t "$LATEST" && echo "gzip OK"
gunzip -c "$LATEST" | head -20            # Expect: pg_dump SQL header / statements

# 4. Restore into the TEST database (safe target) and verify
gunzip -c "$LATEST" \
  | docker exec -i bike-rental-postgres psql -U postgres -d "$TESTDB"
docker exec bike-rental-postgres psql -U postgres -d "$TESTDB" \
  -c "\dt"                                 # Expect: the bikerental tables appear

# 5. The scheduled timer is active
systemctl list-timers 'pg-backup@*'
# Expect: a NEXT run time is listed
```

---

## Section F — Other important checks

### F1. Container health

```bash
docker compose ps
# Expect: STATUS shows "healthy"
```

### F2. Data survives container recreation (but not the volume)

```bash
docker exec bike-rental-postgres psql -U postgres -d "$TESTDB" \
  -c "CREATE TABLE persist_probe (id int); INSERT INTO persist_probe VALUES (42);"
docker compose down          # NOTE: no -v, so the external volume is kept
docker compose up -d
docker exec bike-rental-postgres psql -U postgres -d "$TESTDB" \
  -c "SELECT id FROM persist_probe;"   # Expect: 42 still there
docker exec bike-rental-postgres psql -U postgres -d "$TESTDB" \
  -c "DROP TABLE persist_probe;"
```

### F3. Auto-restart after reboot

```bash
sudo reboot
# After it comes back:
docker compose ps            # Expect: postgres healthy again (restart: always)
```

### F4. Firewall surface

```bash
sudo ufw status verbose
# Expect: only the DB port open to Anywhere; SSH limited to the LAN subnet

# From the external machine — only the forwarded port should answer
nc -zv "$EXTHOST" "$EXTPORT"   # Expect: succeeded
nc -zv "$EXTHOST" 22           # Expect: timed out / refused (SSH not exposed)
```

### F5. Test-DB isolation

```bash
# A table created in the test DB must not appear in the dev DB
docker exec bike-rental-postgres psql -U bikerental_app_migrator -d "$TESTDB" \
  -c "CREATE TABLE iso_probe (id int);"
docker exec bike-rental-postgres psql -U postgres -d bikerental \
  -c "\dt iso_probe"   # Expect: "Did not find any relation named iso_probe"
docker exec bike-rental-postgres psql -U bikerental_app_migrator -d "$TESTDB" \
  -c "DROP TABLE iso_probe;"
```

---

## Results checklist

| # | Check | Pass |
|---|---|---|
| A1 | app: DML ok, CREATE/DROP/CREATE ROLE denied | ☐ |
| A2 | liquibase: DDL ok, CREATE ROLE denied | ☐ |
| A3 | app_migrator: DDL + DML ok | ☐ |
| A4 | admin: full + CREATEROLE ok | ☐ |
| A5 | app inherits DML on admin-created tables | ☐ |
| B1 | admin + app reachable from LAN (via Pi IP) | ☐ |
| B2 | app/liquibase/migrator reachable from internet; **admin rejected** | ☐ |
| B3 | non-TLS connection rejected | ☐ |
| B4 | superuser rejected over TCP | ☐ |
| B5 | live connection is TLS-encrypted | ☐ |
| B6 | passwords stored as SCRAM-SHA-256 | ☐ |
| C | data files in /data/postgres on the NVMe; writes land there | ☐ |
| D | logs written to host folder, readable without sudo; real client IP logged | ☐ |
| E | backup created, owned by operator, valid, restorable; timer active | ☐ |
| F1–F5 | healthy; persists across recreate; restarts after reboot; firewall tight; DB isolation | ☐ |
