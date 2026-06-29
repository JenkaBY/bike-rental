#!/usr/bin/env bash
set -Eeuo pipefail

# Installs the PostgreSQL backup service + daily timer, substituting your
# username into the unit file automatically. Run as your normal user:
#
#   ./systemd/install-backup-timer.sh            # uses the current user
#   ./systemd/install-backup-timer.sh someuser   # or an explicit username
#
# Re-run any time to update the installed unit (e.g. after changing the user or
# the backup schedule).

TARGET_USER="${1:-${SUDO_USER:-$USER}}"
SRC_DIR="$(cd "$(dirname "$0")" && pwd)"
UNIT_DIR=/etc/systemd/system

echo "Installing pg-backup timer for user '${TARGET_USER}'."

# Substitute __USER__ in the service unit; the timer needs no substitution.
sed "s/__USER__/${TARGET_USER}/g" "${SRC_DIR}/pg-backup.service" \
    | sudo tee "${UNIT_DIR}/pg-backup.service" >/dev/null
sudo cp "${SRC_DIR}/pg-backup.timer" "${UNIT_DIR}/pg-backup.timer"

sudo systemctl daemon-reload
sudo systemctl enable --now pg-backup.timer

echo "Done. Scheduled runs:"
systemctl list-timers pg-backup.timer
