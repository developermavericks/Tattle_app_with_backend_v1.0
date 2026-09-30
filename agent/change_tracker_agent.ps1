# Tattle Codebase Change Tracker Agent (PowerShell)
$logFile = "DEVELOPMENT_LOG.md"
$timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"

$status = git status --short
$diff = git diff --stat

if (-not $status) {
    Write-Host "No changes detected in working tree."
    exit 0
}

$entry = @"

## 📝 Update Log - $timestamp

### Modified / Added Files:
```text
$status
```

### Changes Statistics:
```text
$diff
```
"@

if (-not (Test-Path $logFile)) {
    "# 📜 Tattle Application Development & Change Log`n`nThis file is automatically updated by the codebase Change Tracker Agent.`n" | Out-File -FilePath $logFile -Encoding utf8
}

$entry | Out-File -FilePath $logFile -Append -Encoding utf8
Write-Host "✅ Recorded changes in $logFile at $timestamp"
