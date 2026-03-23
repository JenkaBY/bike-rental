<#
.SYNOPSIS
    Aggregates all dependabot branches into a single upgrade branch via cherry-pick.

.DESCRIPTION
    1. Fetches all remote branches.
    2. Finds branches matching the dependabot/* pattern.
    3. Creates or resets the 'upgrade' branch from the specified base branch.
    4. Cherry-picks each new commit from every dependabot branch using
       the '-X theirs' strategy to auto-resolve conflicts.
    5. Prints a summary of succeeded, skipped, and failed branches.

.PARAMETER BaseBranch
    The branch to base 'upgrade' on. Defaults to 'main'.

.PARAMETER UpgradeBranch
    The name of the aggregation branch. Defaults to 'upgrade'.

.EXAMPLE
    .\merge-dependabot-upgrades.ps1
    .\merge-dependabot-upgrades.ps1 -BaseBranch develop -UpgradeBranch all-upgrades
#>
[CmdletBinding()]
param(
    [string]$BaseBranch    = "main",
    [string]$UpgradeBranch = "upgrade"
)

$DEPENDABOT_PREFIX = "dependabot/"

function Write-Info    { param($msg) Write-Host "[INFO]    $msg" -ForegroundColor Cyan }
function Write-Success { param($msg) Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Warn    { param($msg) Write-Host "[WARN]    $msg" -ForegroundColor Yellow }
function Write-Err     { param($msg) Write-Host "[ERROR]   $msg" -ForegroundColor Red }
function Write-Header  { param($msg) Write-Host "`n$msg" -ForegroundColor White }

function Invoke-Git {
    param([string[]]$GitArgs)
    $output = & git @GitArgs 2>$null
    return $output
}

$succeeded = [System.Collections.Generic.List[string]]::new()
$failed    = [System.Collections.Generic.List[string]]::new()
$skipped   = [System.Collections.Generic.List[string]]::new()

Write-Header "=== Dependabot Upgrade Aggregator ==="
Write-Info   "Base branch    : $BaseBranch"
Write-Info   "Upgrade branch : $UpgradeBranch"

Write-Header "--- Step 1: Fetch remote branches ---"
Invoke-Git @("fetch", "--all", "--prune") | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Err "git fetch failed"; exit 1 }
Write-Success "Fetch completed."

Write-Header "--- Step 2: Discover dependabot branches ---"
[string[]]$dependabotBranches = @(
    Invoke-Git @("branch", "-r") |
        Where-Object { $_ -match "origin/$DEPENDABOT_PREFIX" } |
        ForEach-Object { ($_ -replace '.*origin/', '').Trim() } |
        Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
)

if ($dependabotBranches.Length -eq 0) {
    Write-Warn "No dependabot branches found on remote. Nothing to do."
    exit 0
}

Write-Info "Found $($dependabotBranches.Length) branch(es):"
$dependabotBranches | ForEach-Object { Write-Host "    - $_" }

Write-Header "--- Step 3: Check working tree ---"
[string[]]$dirtyLines = @(Invoke-Git @("status", "--porcelain") | Where-Object { $_ -notmatch '^\?\?' })
if ($dirtyLines.Length -gt 0) {
    Write-Err "Working tree has staged or unstaged changes. Commit or stash them first."
    $dirtyLines | ForEach-Object { Write-Host "  $_" -ForegroundColor DarkYellow }
    exit 1
}
Write-Success "Working tree is clean (untracked files are allowed)."

Write-Header "--- Step 4: Prepare base branch ---"
Invoke-Git @("checkout", $BaseBranch) | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Err "Cannot checkout $BaseBranch"; exit 1 }
Invoke-Git @("pull", "origin", $BaseBranch) | Out-Null
if ($LASTEXITCODE -ne 0) { Write-Err "Cannot pull $BaseBranch"; exit 1 }
Write-Success "Base branch '$BaseBranch' is up to date."

Write-Header "--- Step 5: Prepare upgrade branch ---"
[string[]]$branchListResult = @(Invoke-Git @("branch", "--list", $UpgradeBranch))
if ($branchListResult.Length -gt 0) {
    Write-Warn "Branch '$UpgradeBranch' exists - resetting to '$BaseBranch'."
    Invoke-Git @("checkout", $UpgradeBranch) | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Err "Cannot checkout $UpgradeBranch"; exit 1 }
    Invoke-Git @("reset", "--hard", "origin/$BaseBranch") | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Err "Cannot reset $UpgradeBranch"; exit 1 }
} else {
    Write-Info "Creating branch '$UpgradeBranch' from '$BaseBranch'."
    Invoke-Git @("checkout", "-b", $UpgradeBranch) | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Err "Cannot create $UpgradeBranch"; exit 1 }
}
Write-Success "Upgrade branch '$UpgradeBranch' is ready."

Write-Header "--- Step 6: Cherry-pick dependabot commits ---"
foreach ($branch in $dependabotBranches) {
    if ([string]::IsNullOrWhiteSpace($branch)) { continue }

    Write-Info "Processing: $branch"

    [string[]]$commits = @(
        Invoke-Git @("log", "--no-merges", "--reverse", "--format=%H", "origin/$BaseBranch..origin/$branch") |
            Where-Object { $_ -match '^[0-9a-f]{40}$' }
    )

    if ($commits.Length -eq 0) {
        Write-Warn "  No new commits in '$branch' - skipping."
        $skipped.Add($branch)
        continue
    }

    $branchOk = $true
    $failedCommit = ""

    foreach ($commit in $commits) {
        if ([string]::IsNullOrWhiteSpace($commit)) { continue }

        $shortLog = (Invoke-Git @("log", "--format=%h %s", "-1", $commit)) -join ""
        Write-Info "  Cherry-picking: $shortLog"

        Invoke-Git @("cherry-pick", "-X", "theirs", $commit, "--allow-empty") | Out-Null
        if ($LASTEXITCODE -ne 0) {
            Write-Err "  Conflict on: $shortLog - aborting."
            Invoke-Git @("cherry-pick", "--abort") | Out-Null
            $branchOk = $false
            $failedCommit = $commit
            break
        }
        Write-Success "    Applied: $shortLog"
    }

    if ($branchOk) {
        $succeeded.Add($branch)
        Write-Success "  Branch '$branch' applied successfully."
    } else {
        $failed.Add("$branch (failed at $failedCommit)")
        Write-Err "  Branch '$branch' FAILED - rolled back cherry-pick."
    }
}

Write-Header "=== Summary ==="
Write-Host "Succeeded ($($succeeded.Count)):" -ForegroundColor Green
$succeeded | ForEach-Object { Write-Host "    [OK] $_" -ForegroundColor Green }

Write-Host "Skipped ($($skipped.Count)):" -ForegroundColor Yellow
$skipped | ForEach-Object { Write-Host "    [SKIP] $_" -ForegroundColor Yellow }

Write-Host "Failed ($($failed.Count)):" -ForegroundColor Red
$failed | ForEach-Object { Write-Host "    [FAIL] $_" -ForegroundColor Red }

if ($failed.Count -gt 0) {
    Write-Err "Some branches failed. Inspect conflicts and re-run or resolve manually."
    exit 1
}

Write-Success "Upgrade branch '$UpgradeBranch' is ready. Push it with:"
Write-Host "    git push origin $UpgradeBranch --force" -ForegroundColor Cyan
exit 0

