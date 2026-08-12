/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { UserPreferences } from "../types";
import { Logo } from "./Logo";
import {
  Bell,
  Check,
  ChevronRight,
  Shield,
  Trash2,
  Globe,
  Palette,
  BarChart3,
  Flame,
  User,
  Activity,
  Rocket,
  Dna,
  TrendingUp,
  Fingerprint
} from "lucide-react";
import { motion } from "motion/react";

interface SettingsProps {
  preferences: UserPreferences;
  onUpdatePreferences: (prefs: UserPreferences) => void;
  onClose?: () => void;
  onPurgeData: () => void;
}

export const Settings: React.FC<SettingsProps> = ({
  preferences,
  onUpdatePreferences,
  onClose,
  onPurgeData
}) => {
  const [dailyBrief, setDailyBrief] = useState(preferences.notifications.dailyBriefings);
  const [breakingAlert, setBreakingAlert] = useState(preferences.notifications.breakingAlerts);
  const [streakAlert, setStreakAlert] = useState(preferences.notifications.streaks);
  const [sensitivity, setSensitivity] = useState<"Strict" | "Standard" | "Minimal">(preferences.sensitivity);
  const [selectedInterests, setSelectedInterests] = useState<string[]>(preferences.interests);

  const handleToggleNotification = (type: "daily" | "breaking" | "streaks") => {
    const updated = { ...preferences.notifications };
    if (type === "daily") {
      updated.dailyBriefings = !dailyBrief;
      setDailyBrief(!dailyBrief);
    } else if (type === "breaking") {
      updated.breakingAlerts = !breakingAlert;
      setBreakingAlert(!breakingAlert);
    } else {
      updated.streaks = !streakAlert;
      setStreakAlert(!streakAlert);
    }

    onUpdatePreferences({
      ...preferences,
      notifications: updated
    });
  };

  const handleToggleInterest = (id: string) => {
    let updated: string[];
    if (selectedInterests.includes(id)) {
      updated = selectedInterests.filter((x) => x !== id);
    } else {
      updated = [...selectedInterests, id];
    }
    setSelectedInterests(updated);
    onUpdatePreferences({
      ...preferences,
      interests: updated
    });
  };

  const handleUpdateSensitivity = (val: "Strict" | "Standard" | "Minimal") => {
    setSensitivity(val);
    onUpdatePreferences({
      ...preferences,
      sensitivity: val
    });
  };

  // Interest Profile grid items as shown in image 6
  const profileInterests = [
    { id: "Aerospace", label: "Aerospace", icon: <Rocket className="h-6 w-6" /> },
    { id: "Genomics", label: "Genomics", icon: <Dna className="h-6 w-6" /> },
    { id: "Markets", label: "Markets", icon: <TrendingUp className="h-6 w-6" /> },
    { id: "Cybersecurity", label: "Cybersecurity", icon: <Shield className="h-6 w-6" /> }
  ];

  return (
    <div className="w-full h-full bg-background overflow-y-auto pb-32 px-6 font-sans">
      <div className="max-w-xl mx-auto space-y-8 pt-6">
        
        {/* Header Section */}
        <section className="space-y-1">
          <h2 className="text-3xl font-extrabold tracking-tight text-white">Settings</h2>
          <p className="text-sm font-medium text-text-muted">
            Configure your intelligence feed and interface.
          </p>
        </section>

        {/* Notification Controls */}
        <section className="space-y-3">
          <h3 className="text-[10px] font-mono font-extrabold uppercase tracking-widest text-outline">
            Notification Controls
          </h3>
          
          <div className="glass-panel p-5 rounded-2xl space-y-5 border border-white/5">
            {/* Daily briefings */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-bold text-white">Daily Briefings</p>
                <p className="text-[11px] text-text-muted font-medium">
                  Morning summaries of critical news.
                </p>
              </div>
              
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={dailyBrief}
                  onChange={() => handleToggleNotification("daily")}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary" />
              </label>
            </div>

            {/* Breaking Alerts */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-bold text-white">Breaking Alerts</p>
                <p className="text-[11px] text-text-muted font-medium">
                  Real-time push for world-altering events.
                </p>
              </div>
              
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={breakingAlert}
                  onChange={() => handleToggleNotification("breaking")}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary" />
              </label>
            </div>

            {/* Streaks */}
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-bold text-white">Streaks & Milestones</p>
                <p className="text-[11px] text-text-muted font-medium">
                  Nudges to maintain your reading pulse.
                </p>
              </div>
              
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={streakAlert}
                  onChange={() => handleToggleNotification("streaks")}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-white/10 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary" />
              </label>
            </div>
          </div>
        </section>

        {/* Interest Profile Tuning matching image 6 */}
        <section className="space-y-3">
          <h3 className="text-[10px] font-mono font-extrabold uppercase tracking-widest text-outline">
            Interest Profile Tuning
          </h3>
          
          <div className="grid grid-cols-2 gap-4">
            {profileInterests.map((item) => {
              const isActive = selectedInterests.includes(item.id);
              return (
                <button
                  key={item.id}
                  onClick={() => handleToggleInterest(item.id)}
                  className={`glass-panel p-5 rounded-2xl flex flex-col items-center justify-center text-center gap-2 border transition-all duration-300 cursor-pointer ${
                    isActive
                      ? "border-primary bg-primary/10 text-primary scale-98 shadow-[0_0_12px_rgba(69,162,158,0.15)]"
                      : "border-white/5 hover:border-white/10 text-outline hover:text-white"
                  }`}
                >
                  <div className={`transition-transform duration-300 ${isActive ? "scale-110 text-primary" : "text-outline"}`}>
                    {item.icon}
                  </div>
                  <span className="text-xs font-bold font-sans">{item.label}</span>
                </button>
              );
            })}
          </div>
        </section>

        {/* Preferences submenus */}
        <section className="space-y-3">
          <h3 className="text-[10px] font-mono font-extrabold uppercase tracking-widest text-outline">
            Preferences
          </h3>
          
          <div className="glass-panel rounded-2xl overflow-hidden divide-y divide-white/5 border border-white/5">
            {/* Language */}
            <button className="w-full flex items-center justify-between p-4 hover:bg-white/[0.02] transition-colors text-left group cursor-pointer">
              <div className="flex items-center gap-4">
                <Globe className="h-5 w-5 text-outline group-hover:text-primary transition-colors" />
                <div>
                  <p className="text-sm font-bold text-white">Language & Localization</p>
                  <p className="text-[11px] text-text-muted font-medium">
                    {preferences.language} (US), UTC -5
                  </p>
                </div>
              </div>
              <ChevronRight className="h-4 w-4 text-outline" />
            </button>

            {/* Appearance */}
            <button className="w-full flex items-center justify-between p-4 hover:bg-white/[0.02] transition-colors text-left group cursor-pointer">
              <div className="flex items-center gap-4">
                <Palette className="h-5 w-5 text-outline group-hover:text-primary transition-colors" />
                <div>
                  <p className="text-sm font-bold text-white">Appearance & Typography</p>
                  <p className="text-[11px] text-text-muted font-medium">Cyber-Urban, Geist Mono</p>
                </div>
              </div>
              <ChevronRight className="h-4 w-4 text-outline" />
            </button>

            {/* Reading analytics */}
            <button className="w-full flex items-center justify-between p-4 hover:bg-white/[0.02] transition-colors text-left group cursor-pointer">
              <div className="flex items-center gap-4">
                <BarChart3 className="h-5 w-5 text-outline group-hover:text-primary transition-colors" />
                <div>
                  <p className="text-sm font-bold text-white">Reading Analytics & History</p>
                  <p className="text-[11px] text-text-muted font-medium">
                    {(preferences.totalCardsRead * 0.4 + 1.2).toFixed(1)} hrs total reading time
                  </p>
                </div>
              </div>
              <ChevronRight className="h-4 w-4 text-outline" />
            </button>
          </div>
        </section>

        {/* Content Sensitivity Panel matching image 6 */}
        <section className="space-y-3">
          <h3 className="text-[10px] font-mono font-extrabold uppercase tracking-widest text-outline">
            Content Controls
          </h3>
          
          <div className="glass-panel p-5 rounded-2xl border border-white/5 space-y-4">
            <div className="flex items-start gap-4">
              <Shield className="h-5 w-5 text-secondary shrink-0 mt-0.5" />
              <div>
                <p className="text-sm font-bold text-white">Content Sensitivity Panel</p>
                <p className="text-[11px] text-text-muted font-medium">
                  Blur graphic imagery and trigger warnings.
                </p>
              </div>
            </div>

            {/* Pill selector buttons block */}
            <div className="bg-surface-dim/80 border border-white/5 rounded-xl p-1 flex gap-1">
              {(["Strict", "Standard", "Minimal"] as const).map((mode) => {
                const isSelected = sensitivity === mode;
                return (
                  <button
                    key={mode}
                    onClick={() => handleUpdateSensitivity(mode)}
                    className={`flex-1 py-2 text-xs font-bold rounded-lg transition-all cursor-pointer ${
                      isSelected
                        ? "bg-primary/10 text-primary shadow-sm"
                        : "text-outline hover:text-white"
                    }`}
                  >
                    {mode}
                  </button>
                );
              })}
            </div>
          </div>
        </section>

        {/* Privacy Purge Zone */}
        <section className="pt-4">
          <button
            onClick={() => {
              if (window.confirm("Are you absolutely sure you want to purge all your personal news profile tuning, streaks, bookmarks, and local data? This is irreversible.")) {
                onPurgeData();
              }
            }}
            className="w-full py-4 rounded-xl border border-error/30 bg-error/5 hover:bg-error/10 transition-colors flex items-center justify-center gap-2.5 text-error font-bold font-sans text-xs uppercase tracking-widest cursor-pointer"
          >
            <Trash2 className="h-4 w-4" />
            <span>Privacy & Data Purge</span>
          </button>
          
          <p className="text-center text-[10px] font-bold font-mono text-outline uppercase tracking-wider mt-4">
            TATTLE v2.4.1 - Intelligence Authenticated
          </p>
        </section>

      </div>
    </div>
  );
};
