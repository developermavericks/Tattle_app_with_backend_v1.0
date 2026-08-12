/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { UserPreferences } from "../types";
import { Logo } from "./Logo";
import { ArrowRight, ChevronRight, Bell, Volume2, ShieldAlert, Globe, Activity, Check } from "lucide-react";
import { motion, AnimatePresence } from "motion/react";

interface OnboardingProps {
  onComplete: (preferences: UserPreferences) => void;
}

export const Onboarding: React.FC<OnboardingProps> = ({ onComplete }) => {
  const [step, setStep] = useState<number>(1);
  const [language, setLanguage] = useState<string>("English");
  const [ageGroup, setAgeGroup] = useState<string>("18-24");
  const [selectedInterests, setSelectedInterests] = useState<string[]>([]);
  const [notificationsEnabled, setNotificationsEnabled] = useState<boolean | null>(null);

  // Interest Categories as per specs
  const interestsList = [
    { id: "Tech", label: "Tech", icon: "💻" },
    { id: "AI", label: "AI & Robotics", icon: "🧠" },
    { id: "Money & Business", label: "Money & Startups", icon: "📈" },
    { id: "Pop Culture", label: "Pop Culture", icon: "🍿" },
    { id: "Entertainment", label: "Entertainment", icon: "🎬" },
    { id: "Sports", label: "Sports", icon: "⚽" },
    { id: "Science & Space", label: "Science & Space", icon: "🚀" },
    { id: "Climate & Environment", label: "Climate", icon: "🌍" },
    { id: "Health & Wellness", label: "Wellness", icon: "🧘" },
    { id: "Gaming", label: "Gaming", icon: "🎮" },
    { id: "Fashion & Beauty", label: "Fashion", icon: "🧥" },
    { id: "Creator Economy", label: "Creator Economy", icon: "🤳" },
    { id: "World News", label: "World News", icon: "🗺️" }
  ];

  const handleToggleInterest = (id: string) => {
    if (selectedInterests.includes(id)) {
      setSelectedInterests(selectedInterests.filter((x) => x !== id));
    } else {
      setSelectedInterests([...selectedInterests, id]);
    }
  };

  const handleNext = () => {
    if (step < 4) {
      setStep(step + 1);
    } else {
      // Done! Create complete preferences
      const prefs: UserPreferences = {
        isOnboarded: true,
        ageGroup,
        interests: selectedInterests.length >= 3 ? selectedInterests : ["Tech", "AI", "Pop Culture"],
        language,
        notifications: {
          dailyBriefings: notificationsEnabled === true,
          breakingAlerts: notificationsEnabled === true,
          streaks: notificationsEnabled === true,
        },
        streak: 1,
        totalCardsRead: 0,
        readHistory: [],
        bookmarks: [],
        reactions: {},
        adFreeUntil: null,
        sensitivity: ageGroup === "13-15" || ageGroup === "16-17" ? "Strict" : "Standard",
        lastReadDate: new Date().toISOString().split('T')[0],
      };
      onComplete(prefs);
    }
  };

  const handleBack = () => {
    if (step > 1) setStep(step - 1);
  };

  // Step Renders
  return (
    <div className="relative w-full h-screen bg-background overflow-hidden flex flex-col select-none text-white font-sans">
      
      {/* Top persistent progress header */}
      <header className="fixed top-0 left-0 w-full z-50 bg-background/90 backdrop-blur-md px-6 pt-5 pb-3 border-b border-white/5">
        <div className="flex flex-col gap-2 w-full max-w-md mx-auto">
          <div className="flex justify-between items-center">
            <div className="flex items-center gap-1.5">
              {step > 1 && (
                <button 
                  onClick={handleBack} 
                  className="text-xs text-outline hover:text-white mr-2 transition-colors cursor-pointer"
                >
                  Back
                </button>
              )}
              <span className="text-xs font-bold font-mono text-primary uppercase tracking-widest">
                ONBOARDING
              </span>
            </div>
            <span className="text-xs font-mono font-bold text-outline uppercase tracking-wider">
              Step {step} of 4
            </span>
          </div>
          
          {/* Glowing segmented progress bar */}
          <div className="flex gap-2 w-full h-1 bg-surface-container rounded-full overflow-hidden">
            <div className={`h-full bg-primary rounded-full transition-all duration-500 ${step >= 1 ? "w-1/4 shadow-[0_0_8px_#45A29E]" : "w-0"}`} />
            <div className={`h-full bg-primary rounded-full transition-all duration-500 ${step >= 2 ? "w-1/4 shadow-[0_0_8px_#45A29E]" : "w-0"}`} />
            <div className={`h-full bg-primary rounded-full transition-all duration-500 ${step >= 3 ? "w-1/4 shadow-[0_0_8px_#45A29E]" : "w-0"}`} />
            <div className={`h-full bg-primary rounded-full transition-all duration-500 ${step >= 4 ? "w-1/4 shadow-[0_0_8px_#45A29E]" : "w-0"}`} />
          </div>
        </div>
      </header>

      {/* Main step container */}
      <main className="flex-grow pt-24 pb-32 overflow-y-auto px-6 max-w-md mx-auto w-full flex flex-col justify-center">
        <AnimatePresence mode="wait">
          
          {/* STEP 1: LANGUAGE SELECTION */}
          {step === 1 && (
            <motion.div
              key="step1"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="w-full flex flex-col space-y-6"
            >
              {/* Hero header block */}
              <div className="relative rounded-2xl overflow-hidden min-h-[160px] flex items-end p-5 border border-white/5 shadow-2xl">
                <div className="absolute inset-0 z-0">
                  <img
                    alt="Cyberpunk street cityscape"
                    className="w-full h-full object-cover grayscale opacity-30 contrast-125"
                    src="https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=600&auto=format&fit=crop"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-background via-background/40 to-transparent" />
                </div>
                <div className="relative z-10">
                  <h2 className="text-2xl font-extrabold tracking-tight mb-1 text-white">
                    Select your language
                  </h2>
                  <p className="text-text-muted text-xs font-medium max-w-[250px]">
                    Choose the primary language for your daily news digest.
                  </p>
                </div>
              </div>

              {/* Language list options */}
              <div className="flex flex-col gap-3">
                {[
                  { name: "English", note: "System Default" },
                  { name: "Español", note: "Spanish" },
                  { name: "Français", note: "French" },
                  { name: "Deutsch", note: "German" }
                ].map((lang) => {
                  const isActive = language === lang.name;
                  return (
                    <button
                      key={lang.name}
                      onClick={() => setLanguage(lang.name)}
                      className={`w-full flex items-center justify-between p-4 rounded-xl border transition-all duration-300 text-left cursor-pointer group ${
                        isActive 
                          ? "border-primary bg-primary/10 text-white" 
                          : "border-white/5 bg-surface/50 hover:bg-surface text-on-surface"
                      }`}
                    >
                      <div className="flex flex-col">
                        <span className="font-bold text-base">{lang.name}</span>
                        <span className="text-[11px] text-text-muted">{lang.note}</span>
                      </div>
                      
                      {/* Stylized custom radio button */}
                      <div className={`w-5 h-5 rounded-full border flex items-center justify-center transition-all ${
                        isActive ? "border-primary" : "border-outline"
                      }`}>
                        <div className={`w-2.5 h-2.5 rounded-full transition-all ${
                          isActive ? "bg-primary scale-100" : "bg-transparent scale-0"
                        }`} />
                      </div>
                    </button>
                  );
                })}
              </div>
            </motion.div>
          )}

          {/* STEP 2: AGE BRACKETS */}
          {step === 2 && (
            <motion.div
              key="step2"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="w-full flex flex-col space-y-6"
            >
              {/* Info Block */}
              <div className="text-center md:text-left">
                <h2 className="text-2xl font-extrabold tracking-tight mb-2 text-white">
                  How old are you?
                </h2>
                <p className="text-text-muted text-sm font-medium">
                  So we can keep your feed age-appropriate. That's it.
                </p>
              </div>

              {/* Age list selections */}
              <div className="flex flex-col gap-3">
                {[
                  { range: "13-15", note: "Content safety filters: STRICT" },
                  { range: "16-17", note: "Content safety filters: STRICT" },
                  { range: "18-24", note: "Full Feed Access, Standard filters" },
                  { range: "25+", note: "Full Feed Access, Minimal filters" }
                ].map((bracket) => {
                  const isActive = ageGroup === bracket.range;
                  return (
                    <button
                      key={bracket.range}
                      onClick={() => setAgeGroup(bracket.range)}
                      className={`w-full flex items-center justify-between p-5 rounded-xl border transition-all duration-300 text-left cursor-pointer ${
                        isActive 
                          ? "border-primary bg-primary/10 text-white" 
                          : "border-white/5 bg-surface/50 hover:bg-surface text-on-surface"
                      }`}
                    >
                      <div className="flex flex-col">
                        <span className="font-extrabold text-lg tracking-wide">{bracket.range}</span>
                        <span className="text-xs text-text-muted mt-0.5">{bracket.note}</span>
                      </div>
                      
                      <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all ${
                        isActive ? "border-primary" : "border-outline"
                      }`}>
                        <div className={`w-3 h-3 rounded-full transition-all ${
                          isActive ? "bg-primary scale-100" : "bg-transparent scale-0"
                        }`} />
                      </div>
                    </button>
                  );
                })}
              </div>
            </motion.div>
          )}

          {/* STEP 3: INTEREST MATRIX */}
          {step === 3 && (
            <motion.div
              key="step3"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="w-full flex flex-col space-y-4"
            >
              <div className="text-center md:text-left">
                <h2 className="text-2xl font-extrabold tracking-tight mb-1 text-white">
                  Pick what you're into
                </h2>
                <p className="text-text-muted text-xs font-medium">
                  Select a minimum of three categories. Change this anytime in Settings.
                </p>
              </div>

              {/* Grid matrix of categories */}
              <div className="grid grid-cols-2 gap-2.5 max-h-[380px] overflow-y-auto pr-1 no-scrollbar py-2">
                {interestsList.map((interest) => {
                  const isSelected = selectedInterests.includes(interest.id);
                  return (
                    <button
                      key={interest.id}
                      onClick={() => handleToggleInterest(interest.id)}
                      className={`flex items-center gap-2.5 p-3.5 rounded-xl border transition-all duration-200 text-left cursor-pointer ${
                        isSelected
                          ? "border-primary bg-primary/15 text-white shadow-[0_0_12px_rgba(69,162,158,0.15)]"
                          : "border-white/5 bg-surface/50 hover:bg-surface text-on-surface-variant"
                      }`}
                    >
                      <span className="text-xl">{interest.icon}</span>
                      <span className="font-bold text-[13px] tracking-wide truncate">
                        {interest.label}
                      </span>
                    </button>
                  );
                })}
              </div>

              {/* Selected Count Indicator */}
              <div className="text-center py-1">
                <span className="text-[11px] font-mono tracking-widest font-bold text-text-muted uppercase">
                  {selectedInterests.length} of 3 required selected
                </span>
              </div>
            </motion.div>
          )}

          {/* STEP 4: PUSH NOTIFICATIONS OPT-IN */}
          {step === 4 && (
            <motion.div
              key="step4"
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              transition={{ duration: 0.4 }}
              className="w-full flex flex-col items-center text-center space-y-6"
            >
              {/* Notification Circular Icon Container */}
              <div className="relative group">
                <div className="absolute inset-0 bg-primary/10 blur-[40px] rounded-full scale-125 animate-pulse" />
                
                <div className="relative w-36 h-36 flex items-center justify-center glass-panel rounded-full border border-primary/20">
                  <Bell className="h-16 w-16 text-primary animate-bounce" style={{ animationDuration: "3s" }} />
                  
                  {/* Floating badges */}
                  <div className="absolute -top-3 -right-6 glass-panel px-3 py-1 rounded-full border border-secondary/20 animate-pulse">
                    <span className="text-[10px] font-mono font-bold text-secondary tracking-widest uppercase">
                      Breaking
                    </span>
                  </div>
                  <div className="absolute top-1/2 -left-10 glass-panel px-3 py-1 rounded-full border border-primary/20 animate-pulse" style={{ animationDelay: "1s" }}>
                    <span className="text-[10px] font-mono font-bold text-primary tracking-widest uppercase">
                      Daily Digest
                    </span>
                  </div>
                </div>
              </div>

              {/* Headline & Subtext */}
              <div className="max-w-xs space-y-1">
                <h2 className="text-2xl font-extrabold tracking-tight text-white">
                  Stay in the loop
                </h2>
                <p className="text-text-muted text-xs leading-relaxed font-medium">
                  Get notified about daily digests and breaking news so you never miss a beat.
                </p>
              </div>

              {/* Preview card */}
              <div className="w-full max-w-xs rotate-1 hover:rotate-0 transition-all duration-300 transform">
                <div className="glass-panel p-4 rounded-xl text-left border border-white/10 relative overflow-hidden shadow-2xl">
                  <div className="flex items-start gap-3">
                    <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center font-extrabold text-black text-sm shrink-0">
                      T
                    </div>
                    <div className="flex-grow">
                      <div className="flex justify-between items-center text-[10px] font-mono text-text-muted font-bold tracking-wider mb-0.5">
                        <span>TATTLE</span>
                        <span>NOW</span>
                      </div>
                      <h4 className="text-xs font-bold text-white mb-0.5 leading-tight">
                        Breaking: Major Tech Shift
                      </h4>
                      <p className="text-[11px] text-text-muted line-clamp-1 leading-normal">
                        The landscape of digital ownership is changing in Sector 7...
                      </p>
                    </div>
                  </div>
                  {/* Gloss reflection shimmer */}
                  <div className="absolute top-0 left-0 w-full h-full bg-gradient-to-br from-white/5 to-transparent pointer-events-none" />
                </div>
              </div>
            </motion.div>
          )}

        </AnimatePresence>
      </main>

      {/* Persistent Bottom Action Zone */}
      <footer className="fixed bottom-0 left-0 w-full px-6 pb-10 pt-4 bg-gradient-to-t from-background via-background to-transparent z-40">
        <div className="w-full max-w-md mx-auto flex flex-col gap-3">
          
          {/* Primary CTA */}
          {step === 3 ? (
            <button
              disabled={selectedInterests.length < 3}
              onClick={handleNext}
              className={`w-full h-14 font-semibold text-sm tracking-widest uppercase rounded-full flex items-center justify-center gap-2 transition-all duration-200 cursor-pointer ${
                selectedInterests.length >= 3
                  ? "bg-primary hover:bg-primary-light text-black shadow-lg shadow-primary/25"
                  : "bg-surface-container text-outline opacity-60 cursor-not-allowed"
              }`}
            >
              <span>Next</span>
              <ChevronRight className="h-5 w-5 stroke-[2.5px]" />
            </button>
          ) : step === 4 ? (
            <button
              onClick={() => {
                setNotificationsEnabled(true);
                // Simple delay for haptic success feedback simulation
                setTimeout(() => {
                  const prefs: UserPreferences = {
                    isOnboarded: true,
                    ageGroup,
                    interests: selectedInterests.length >= 3 ? selectedInterests : ["Tech", "AI", "Pop Culture"],
                    language,
                    notifications: {
                      dailyBriefings: true,
                      breakingAlerts: true,
                      streaks: true,
                    },
                    streak: 1,
                    totalCardsRead: 0,
                    readHistory: [],
                    bookmarks: [],
                    reactions: {},
                    adFreeUntil: null,
                    sensitivity: ageGroup === "13-15" || ageGroup === "16-17" ? "Strict" : "Standard",
                    lastReadDate: new Date().toISOString().split('T')[0],
                  };
                  onComplete(prefs);
                }, 400);
              }}
              className="w-full h-14 bg-primary hover:bg-primary-light text-black font-semibold text-sm tracking-widest uppercase rounded-full flex items-center justify-center gap-2 shadow-lg shadow-primary/20 cursor-pointer transition-transform duration-150 active:scale-98"
            >
              <span>Enable Notifications</span>
              <ChevronRight className="h-5 w-5 stroke-[2.5px]" />
            </button>
          ) : (
            <button
              onClick={handleNext}
              className="w-full h-14 bg-primary hover:bg-primary-light text-black font-semibold text-sm tracking-widest uppercase rounded-full flex items-center justify-center gap-2 shadow-lg shadow-primary/20 cursor-pointer transition-transform duration-150 active:scale-98"
            >
              <span>Next</span>
              <ChevronRight className="h-5 w-5 stroke-[2.5px]" />
            </button>
          )}

          {/* Secondary Utility Actions */}
          {step === 4 && (
            <button
              onClick={() => {
                setNotificationsEnabled(false);
                const prefs: UserPreferences = {
                  isOnboarded: true,
                  ageGroup,
                  interests: selectedInterests.length >= 3 ? selectedInterests : ["Tech", "AI", "Pop Culture"],
                  language,
                  notifications: {
                    dailyBriefings: false,
                    breakingAlerts: false,
                    streaks: false,
                  },
                  streak: 1,
                  totalCardsRead: 0,
                  readHistory: [],
                  bookmarks: [],
                  reactions: {},
                  adFreeUntil: null,
                  sensitivity: ageGroup === "13-15" || ageGroup === "16-17" ? "Strict" : "Standard",
                  lastReadDate: new Date().toISOString().split('T')[0],
                };
                onComplete(prefs);
              }}
              className="w-full text-center text-xs font-semibold text-outline hover:text-white transition-colors py-2 cursor-pointer"
            >
              Skip for now
            </button>
          )}
        </div>
      </footer>
    </div>
  );
};
