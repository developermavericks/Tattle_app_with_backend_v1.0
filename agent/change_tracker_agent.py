#!/usr/bin/env python3
"""
===============================================================================
🤖 Tattle Codebase Change Tracker Agent
===============================================================================
An autonomous background agent that continuously monitors, logs, and tracks all
modifications, additions, and deletions in the Tattle codebase.

Features:
  1. Auto-logs changes to DEVELOPMENT_LOG.md with timestamps & file stats.
  2. Background Watcher Mode (--watch) for real-time file monitoring.
  3. Interactive & Command-line Rollback helper (--rollback <file/commit>).
  4. Change Summary Generator (--summary).
===============================================================================
"""

import os
import sys
import time
import subprocess
from datetime import datetime

PROJECT_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOG_FILE = os.path.join(PROJECT_ROOT, "DEVELOPMENT_LOG.md")


def run_git(cmd, cwd=PROJECT_ROOT):
    """Executes a git command and returns output text."""
    try:
        result = subprocess.run(
            ["git"] + cmd,
            cwd=cwd,
            capture_output=True,
            text=True,
            check=True
        )
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        return e.stdout.strip() or e.stderr.strip()
    except Exception as e:
        return f"Error: {e}"


def get_git_branch():
    return run_git(["rev-parse", "--abbrev-ref", "HEAD"]) or "main"


def get_git_status():
    """Returns raw status porcelain text."""
    return run_git(["status", "--porcelain"])


def get_git_diff_stat():
    """Returns diff stat summary."""
    return run_git(["diff", "--stat"])


def parse_status(status_text):
    """Parses status porcelain into categorized files."""
    modified, added, deleted, untracked = [], [], [], []
    for line in status_text.splitlines():
        if not line.strip():
            continue
        code = line[:2]
        filepath = line[3:].strip()
        if "??" in code:
            untracked.append(filepath)
        elif "A" in code or "A " in code or " A" in code:
            added.append(filepath)
        elif "D" in code or "D " in code or " D" in code:
            deleted.append(filepath)
        elif "M" in code or "M " in code or " M" in code:
            modified.append(filepath)
    return {
        "modified": modified,
        "added": added,
        "deleted": deleted,
        "untracked": untracked
    }


def record_changes(note=None):
    """Appends current codebase changes to DEVELOPMENT_LOG.md."""
    status_raw = get_git_status()
    if not status_raw:
        print("ℹ️ No uncommitted changes detected in codebase.")
        return False

    parsed = parse_status(status_raw)
    diff_stat = get_git_diff_stat()
    branch = get_git_branch()
    now_str = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    entry_lines = []
    entry_lines.append(f"\n### ⚡ Auto-Tracked Changes [{now_str}]")
    entry_lines.append(f"* **Branch**: `{branch}`")
    if note:
        entry_lines.append(f"* **Agent Note**: {note}")

    if parsed["modified"]:
        entry_lines.append("\n**Modified Files**:")
        for f in parsed["modified"]:
            entry_lines.append(f"- 📝 `{f}`")

    if parsed["added"] or parsed["untracked"]:
        entry_lines.append("\n**New / Staged Files**:")
        for f in parsed["added"] + parsed["untracked"]:
            entry_lines.append(f"- ✨ `{f}`")

    if parsed["deleted"]:
        entry_lines.append("\n**Deleted Files**:")
        for f in parsed["deleted"]:
            entry_lines.append(f"- 🗑️ `{f}`")

    if diff_stat:
        entry_lines.append("\n**Diff Summary**:")
        entry_lines.append("```")
        entry_lines.append(diff_stat)
        entry_lines.append("```")

    entry_lines.append("\n" + "-" * 50)

    entry_text = "\n".join(entry_lines) + "\n"

    if not os.path.exists(LOG_FILE):
        header = "# Tattle Project - Development & Change Log\n\n"
        with open(LOG_FILE, "w", encoding="utf-8") as f:
            f.write(header + entry_text)
    else:
        with open(LOG_FILE, "a", encoding="utf-8") as f:
            f.write(entry_text)

    print(f"✅ Recorded changes in DEVELOPMENT_LOG.md at {now_str}")
    return True


def show_summary():
    """Prints current tracking summary to terminal."""
    branch = get_git_branch()
    status_raw = get_git_status()
    diff_stat = get_git_diff_stat()

    print("=" * 60)
    print("🤖 TATTLE CHANGE TRACKER AGENT SUMMARY")
    print("=" * 60)
    print(f"Branch: {branch}")
    print(f"Timestamp: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print("-" * 60)
    if status_raw:
        print("Uncommitted File Status:")
        print(status_raw)
        print("-" * 60)
        if diff_stat:
            print("Diff Statistics:")
            print(diff_stat)
    else:
        print("Clean working directory. No pending changes.")
    print("=" * 60)


def rollback_target(target):
    """Restores a specific file or discards changes."""
    print(f"🔄 Initiating rollback for target: {target}")
    if os.path.exists(os.path.join(PROJECT_ROOT, target)) or target in get_git_status():
        out = run_git(["checkout", "--", target])
        print(f"✅ Restored {target}: {out or 'Success'}")
    else:
        print(f"⚠️ Target '{target}' not found in working tree. Attempting git checkout...")
        out = run_git(["checkout", target])
        print(f"Response: {out}")


def watch_mode(interval=5):
    """Continuously monitors for file changes every N seconds."""
    print(f"👀 Change Tracker Agent active in WATCH MODE (interval: {interval}s)...")
    print("Press Ctrl+C to stop.")
    last_status = None
    try:
        while True:
            current_status = get_git_status()
            if current_status and current_status != last_status:
                print("\n🔔 Codebase modification detected!")
                record_changes(note="Background Watcher Detection")
                last_status = current_status
            elif not current_status:
                last_status = None
            time.sleep(interval)
    except KeyboardInterrupt:
        print("\n👋 Watch Mode stopped.")


def main():
    if len(sys.argv) > 1:
        arg = sys.argv[1]
        if arg == "--watch":
            interval = int(sys.argv[2]) if len(sys.argv) > 2 else 5
            watch_mode(interval)
        elif arg == "--summary":
            show_summary()
        elif arg == "--record":
            note = " ".join(sys.argv[2:]) if len(sys.argv) > 2 else "Manual Agent Record"
            record_changes(note)
        elif arg == "--rollback":
            if len(sys.argv) > 2:
                rollback_target(sys.argv[2])
            else:
                print("Usage: python agent/change_tracker_agent.py --rollback <filename_or_commit>")
        else:
            print("Unknown argument. Usage:")
            print("  python agent/change_tracker_agent.py")
            print("  python agent/change_tracker_agent.py --record [note]")
            print("  python agent/change_tracker_agent.py --watch [interval_sec]")
            print("  python agent/change_tracker_agent.py --summary")
            print("  python agent/change_tracker_agent.py --rollback <filepath>")
    else:
        record_changes("Manual Agent Trigger")


if __name__ == "__main__":
    main()
