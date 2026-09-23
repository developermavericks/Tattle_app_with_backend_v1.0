# ===============================================================================
# 🤖 Tattle Codebase Change Tracker Agent (PowerShell Wrapper)
# ===============================================================================

param (
    [switch]$Watch,
    [switch]$Summary,
    [string]$Rollback,
    [string]$Note
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$AgentPy = Join-Path $ScriptDir "change_tracker_agent.py"

if ($Watch) {
    python $AgentPy --watch
} elseif ($Summary) {
    python $AgentPy --summary
} elseif ($Rollback) {
    python $AgentPy --rollback $Rollback
} elseif ($Note) {
    python $AgentPy --record $Note
} else {
    python $AgentPy
}
