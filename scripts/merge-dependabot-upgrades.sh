#!/usr/bin/env bash
set -euo pipefail

BASE_BRANCH="${1:-main}"
UPGRADE_BRANCH="${2:-upgrade}"
DEPENDABOT_PREFIX="dependabot/"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
BOLD='\033[1m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC}    $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC}    $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC}   $1"; }
log_header()  { echo -e "\n${BOLD}$1${NC}"; }

SUCCEEDED=()
FAILED=()
SKIPPED=()

log_header "=== Dependabot Upgrade Aggregator ==="
log_info "Base branch    : ${BASE_BRANCH}"
log_info "Upgrade branch : ${UPGRADE_BRANCH}"

log_header "--- Step 1: Fetch remote branches ---"
git fetch --all --prune
log_success "Fetch completed."

log_header "--- Step 2: Discover dependabot branches ---"
DEPENDABOT_BRANCHES=$(git branch -r \
  | grep "origin/${DEPENDABOT_PREFIX}" \
  | sed 's|.*origin/||' \
  | tr -d ' ')

if [ -z "${DEPENDABOT_BRANCHES}" ]; then
  log_warn "No dependabot branches found on remote. Nothing to do."
  exit 0
fi

log_info "Found branches:"
while IFS= read -r b; do
  echo "    - ${b}"
done <<< "${DEPENDABOT_BRANCHES}"

log_header "--- Step 3: Check working tree ---"
DIRTY=$(git status --porcelain | grep -v "^??" || true)
if [ -n "${DIRTY}" ]; then
  log_error "Working tree has staged or unstaged changes. Commit or stash them before running this script."
  echo "${DIRTY}"
  exit 1
fi
log_success "Working tree is clean (untracked files are allowed)."

log_header "--- Step 4: Prepare base branch ---"
git checkout "${BASE_BRANCH}"
git pull origin "${BASE_BRANCH}"
log_success "Base branch '${BASE_BRANCH}' is up to date."

log_header "--- Step 4: Prepare upgrade branch ---"
if git show-ref --verify --quiet "refs/heads/${UPGRADE_BRANCH}"; then
  log_warn "Branch '${UPGRADE_BRANCH}' exists — resetting to '${BASE_BRANCH}'."
  git checkout "${UPGRADE_BRANCH}"
  git reset --hard "origin/${BASE_BRANCH}"
else
  log_info "Creating branch '${UPGRADE_BRANCH}' from '${BASE_BRANCH}'."
  git checkout -b "${UPGRADE_BRANCH}"
fi
log_success "Upgrade branch '${UPGRADE_BRANCH}' is ready."

log_header "--- Step 5: Cherry-pick dependabot commits ---"
while IFS= read -r BRANCH; do
  [ -z "${BRANCH}" ] && continue

  log_info "Processing: ${BRANCH}"

  COMMITS=$(git log \
    --oneline \
    --no-merges \
    --reverse \
    "origin/${BASE_BRANCH}..origin/${BRANCH}" \
    --format="%H" 2>/dev/null || true)

  if [ -z "${COMMITS}" ]; then
    log_warn "  No new commits in '${BRANCH}' — skipping."
    SKIPPED+=("${BRANCH}")
    continue
  fi

  BRANCH_OK=true
  FAILED_COMMIT=""
  while IFS= read -r COMMIT; do
    [ -z "${COMMIT}" ] && continue
    SHORT=$(git log --format="%h %s" -1 "${COMMIT}")
    log_info "  Cherry-picking: ${SHORT}"
    if git cherry-pick -X theirs "${COMMIT}" --allow-empty 2>/dev/null; then
      log_success "    Applied: ${SHORT}"
    else
      log_error "    Conflict on: ${SHORT} — aborting."
      git cherry-pick --abort 2>/dev/null || true
      BRANCH_OK=false
      FAILED_COMMIT="${COMMIT}"
      break
    fi
  done <<< "${COMMITS}"

  if ${BRANCH_OK}; then
    SUCCEEDED+=("${BRANCH}")
    log_success "  Branch '${BRANCH}' applied successfully."
  else
    FAILED+=("${BRANCH} (failed at ${FAILED_COMMIT})")
    log_error "  Branch '${BRANCH}' FAILED — rolled back cherry-pick."
  fi
done <<< "${DEPENDABOT_BRANCHES}"

log_header "=== Summary ==="
echo -e "${GREEN}Succeeded (${#SUCCEEDED[@]}):${NC}"
for b in "${SUCCEEDED[@]:-}"; do
  [ -n "${b}" ] && echo "    ✅ ${b}"
done

echo -e "${YELLOW}Skipped (${#SKIPPED[@]}):${NC}"
for b in "${SKIPPED[@]:-}"; do
  [ -n "${b}" ] && echo "    ⏭  ${b}"
done

echo -e "${RED}Failed (${#FAILED[@]}):${NC}"
for b in "${FAILED[@]:-}"; do
  [ -n "${b}" ] && echo "    ❌ ${b}"
done

if [ "${#FAILED[@]}" -gt 0 ]; then
  log_error "Some branches failed. Inspect conflicts and re-run or resolve manually."
  exit 1
fi

log_success "Upgrade branch '${UPGRADE_BRANCH}' is ready. Push it with:"
echo "    git push origin ${UPGRADE_BRANCH} --force"
exit 0


