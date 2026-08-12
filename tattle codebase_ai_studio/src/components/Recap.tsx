/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { UserPreferences } from "../types";
import { BookOpen, Flame, Share2, Sparkles, Sliders, Check } from "lucide-react";
import { motion } from "motion/react";

interface RecapProps {
  preferences: UserPreferences;
  onTuneFeed: () => void;
}

export const Recap: React.FC<RecapProps> = ({ preferences, onTuneFeed }) => {
  const [copied, setCopied] = useState<boolean>(false);

  // Stats
  const cardsRead = preferences.totalCardsRead || 15; // default to 15 for visual richness
  const streakDays = preferences.streak || 12;

  const handleShare = () => {
    setCopied(true);
    navigator.clipboard.writeText("https://tattle.news/recap/user-39824");
    setTimeout(() => {
      setCopied(false);
    }, 2000);
  };

  return (
    <div className="w-full h-full bg-background overflow-y-auto pb-32 px-6 font-sans">
      <div className="max-w-md mx-auto space-y-8 pt-6">
        
        {/* Status Header Block */}
        <section className="text-center space-y-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-primary/10 rounded-full border border-primary/20">
            <span className="w-2 h-2 bg-primary rounded-full animate-pulse" />
            <span className="text-[10px] font-mono font-extrabold text-primary uppercase tracking-widest">
              LIMIT REACHED
            </span>
          </div>
          
          <h2 className="text-3xl font-extrabold tracking-tight text-white font-sans">
            Daily Recap
          </h2>
          <p className="text-sm font-medium text-text-muted max-w-[280px] mx-auto leading-relaxed">
            Come back later — new stories drop throughout the day.
          </p>
        </section>

        {/* Metric Grid matching image 3 */}
        <section className="grid grid-cols-2 gap-4">
          {/* Cards Read Card */}
          <div className="glass-card p-5 rounded-2xl flex flex-col items-center justify-center text-center border border-white/5 shadow-lg">
            <BookOpen className="text-primary h-6 w-6 mb-2 stroke-[2.5px]" />
            <span className="text-2xl font-black text-white font-sans leading-none">{cardsRead}</span>
            <span className="text-[10px] font-mono font-extrabold text-text-muted uppercase mt-2 tracking-wider">
              Cards Read
            </span>
          </div>

          {/* Streak Days Card */}
          <div className="glass-card p-5 rounded-2xl flex flex-col items-center justify-center text-center border border-white/5 shadow-lg">
            <Flame className="text-secondary h-6 w-6 mb-2 stroke-[2.5px] fill-secondary" />
            <span className="text-2xl font-black text-white font-sans leading-none">{streakDays} Days</span>
            <span className="text-[10px] font-mono font-extrabold text-text-muted uppercase mt-2 tracking-wider">
              Reading Streak
            </span>
          </div>
        </section>

        {/* Graphical category percentage chart matching image 3 */}
        <section className="glass-card p-6 rounded-2xl border border-white/5 relative overflow-hidden space-y-6 shadow-xl">
          <div className="flex justify-between items-end h-32 gap-5 mb-4">
            {/* Tech - 40% */}
            <div className="flex-1 flex flex-col items-center gap-2">
              <div className="w-full bg-primary-container/10 rounded-t-lg h-full relative flex items-end">
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: "40%" }}
                  transition={{ duration: 1.2, ease: "easeOut" }}
                  className="w-full bg-primary rounded-t-lg neon-glow-primary"
                />
              </div>
              <span className="text-xs font-mono font-extrabold text-white">40%</span>
            </div>

            {/* Pop Culture - 30% */}
            <div className="flex-1 flex flex-col items-center gap-2">
              <div className="w-full bg-secondary-container/10 rounded-t-lg h-full relative flex items-end">
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: "30%" }}
                  transition={{ duration: 1.2, ease: "easeOut", delay: 0.1 }}
                  className="w-full bg-secondary rounded-t-lg neon-glow-secondary"
                />
              </div>
              <span className="text-xs font-mono font-extrabold text-white">30%</span>
            </div>

            {/* Politics - 20% */}
            <div className="flex-1 flex flex-col items-center gap-2">
              <div className="w-full bg-tertiary-container/10 rounded-t-lg h-full relative flex items-end">
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: "20%" }}
                  transition={{ duration: 1.2, ease: "easeOut", delay: 0.2 }}
                  className="w-full bg-tertiary rounded-t-lg"
                />
              </div>
              <span className="text-xs font-mono font-extrabold text-white">20%</span>
            </div>

            {/* Other - 10% */}
            <div className="flex-1 flex flex-col items-center gap-2">
              <div className="w-full bg-white/[0.03] rounded-t-lg h-full relative flex items-end">
                <motion.div
                  initial={{ height: 0 }}
                  animate={{ height: "10%" }}
                  transition={{ duration: 1.2, ease: "easeOut", delay: 0.3 }}
                  className="w-full bg-outline rounded-t-lg"
                />
              </div>
              <span className="text-xs font-mono font-extrabold text-white">10%</span>
            </div>
          </div>

          {/* Color matching legend grid */}
          <div className="grid grid-cols-2 gap-y-2.5 gap-x-4 pt-3 border-t border-white/5">
            <div className="flex items-center gap-2">
              <div className="w-2.5 h-2.5 rounded-full bg-primary shrink-0" />
              <span className="text-xs font-medium text-text-muted">Tech & AI</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-2.5 h-2.5 rounded-full bg-secondary shrink-0" />
              <span className="text-xs font-medium text-text-muted">Pop Culture</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-2.5 h-2.5 rounded-full bg-tertiary shrink-0" />
              <span className="text-xs font-medium text-text-muted">Politics</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-2.5 h-2.5 rounded-full bg-outline shrink-0" />
              <span className="text-xs font-medium text-text-muted">Other Genres</span>
            </div>
          </div>
        </section>

        {/* Primary CTAs matching image 3 */}
        <section className="flex flex-col gap-4">
          <button
            onClick={handleShare}
            className="w-full h-14 bg-primary hover:bg-primary-light text-black font-semibold text-xs tracking-widest uppercase rounded-full flex items-center justify-center gap-2 transition-transform duration-150 active:scale-98 shadow-lg shadow-primary/20 cursor-pointer"
          >
            {copied ? (
              <>
                <Check className="h-4 w-4 stroke-[2.5px]" />
                <span>Copied Link!</span>
              </>
            ) : (
              <>
                <Share2 className="h-4 w-4 stroke-[2.5px]" />
                <span>Share Your Recap</span>
              </>
            )}
          </button>
          
          <button
            onClick={onTuneFeed}
            className="w-full text-center py-2 text-primary hover:text-primary-light font-bold text-xs uppercase tracking-widest transition-colors cursor-pointer flex items-center justify-center gap-2"
          >
            <Sliders className="h-3.5 w-3.5" />
            <span>Tune Your Feed</span>
          </button>
        </section>

      </div>
    </div>
  );
};
