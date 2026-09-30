#!/usr/bin/env python3
import os
import subprocess
import datetime

LOG_FILE = "DEVELOPMENT_LOG.md"

def run_cmd(cmd):
    try:
        res = subprocess.run(cmd, shell=True, capture_output=True, text=True)
        return res.stdout.strip()
    except Exception as e:
        return f"Error running command: {e}"

def log_changes():
    timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    git_status = run_cmd("git status --short")
    git_diff = run_cmd("git diff --stat")

    if not git_status and not git_diff:
        print("No changes detected in working tree.")
        return

    log_entry = f"\n\n## 📝 Update Log - {timestamp}\n\n"
    log_entry += "### Modified / Added Files:\n```text\n"
    log_entry += git_status if git_status else "No modified files listed."
    log_entry += "\n```\n\n"

    if git_diff:
        log_entry += "### Changes Statistics:\n```text\n"
        log_entry += git_diff
        log_entry += "\n```\n"

    mode = "a" if os.path.exists(LOG_FILE) else "w"
    if mode == "w":
        with open(LOG_FILE, "w", encoding="utf-8") as f:
            f.write("# 📜 Tattle Application Development & Change Log\n\nThis file is automatically updated by the codebase Change Tracker Agent.\n")

    with open(LOG_FILE, "a", encoding="utf-8") as f:
        f.write(log_entry)

    print(f"✅ Recorded changes in {LOG_FILE} at {timestamp}")

if __name__ == "__main__":
    log_changes()
