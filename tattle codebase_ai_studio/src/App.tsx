/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from "react";
import { Splash } from "./components/Splash";
import { Onboarding } from "./components/Onboarding";
import { Feed } from "./components/Feed";
import { BriefView } from "./components/BriefView";
import { FullView } from "./components/FullView";
import { Settings } from "./components/Settings";
import { Recap } from "./components/Recap";
import { ErrorView } from "./components/ErrorView";
import { Article, UserPreferences } from "./types";
import { mockArticles } from "./data/news";
import { Zap, Compass, BarChart3, Settings as SettingsIcon, Wifi, WifiOff, Share2, ArrowUpRight, Check, Sparkles } from "lucide-react";
import { AnimatePresence, motion } from "motion/react";

export default function App() {
  const [preferences, setPreferences] = useState<UserPreferences | null>(null);
  const [currentScreen, setCurrentScreen] = useState<"Splash" | "Onboarding" | "Feed" | "Recap" | "Settings" | "Offline">("Splash");
  
  // Navigation states
  const [activeArticle, setActiveArticle] = useState<Article | null>(null);
  const [activeBriefArticle, setActiveBriefArticle] = useState<Article | null>(null);
  
  // Simulation utilities
  const [isOffline, setIsOffline] = useState<boolean>(false);
  const [shareToast, setShareToast] = useState<string | null>(null);
  const [showShareAsset, setShowShareAsset] = useState<Article | null>(null);

  // Load preferences from local storage on mounting
  useEffect(() => {
    try {
      const stored = localStorage.getItem("tattle_user_preferences");
      if (stored) {
        const parsed = JSON.parse(stored) as UserPreferences;
        setPreferences(parsed);
        // Directly route to Feed if onboarded
        if (parsed.isOnboarded) {
          setCurrentScreen("Feed");
        }
      }
    } catch (e) {
      console.error("Failed to parse preferences", e);
    }
  }, []);

  // Update preferences state and local storage
  const handleUpdatePreferences = (updatedPrefs: UserPreferences) => {
    setPreferences(updatedPrefs);
    try {
      localStorage.setItem("tattle_user_preferences", JSON.stringify(updatedPrefs));
    } catch (e) {
      console.error("Failed to save preferences", e);
    }
  };

  const handleCompleteOnboarding = (prefs: UserPreferences) => {
    handleUpdatePreferences(prefs);
    setCurrentScreen("Feed");
  };

  const handlePurgeData = () => {
    try {
      localStorage.removeItem("tattle_user_preferences");
    } catch (e) {}
    setPreferences(null);
    setCurrentScreen("Splash");
  };

  const handleShareArticle = (article: Article) => {
    setShowShareAsset(article);
  };

  const handleTriggerShareCopy = () => {
    setShareToast("Share link copied to clipboard!");
    navigator.clipboard.writeText(`https://tattle.news/share/article-${showShareAsset?.id}`);
    setTimeout(() => {
      setShareToast(null);
      setShowShareAsset(null);
    }, 2000);
  };

  const toggleOfflineSimulation = () => {
    setIsOffline(!isOffline);
    if (!isOffline) {
      setCurrentScreen("Offline");
    } else {
      setCurrentScreen("Feed");
    }
  };

  // Safe default preferences fallback
  const currentPrefs = preferences || {
    isOnboarded: false,
    ageGroup: "18-24",
    interests: ["Tech", "AI", "Pop Culture"],
    language: "English",
    notifications: { dailyBriefings: true, breakingAlerts: true, streaks: false },
    streak: 12,
    totalCardsRead: 124,
    readHistory: [],
    bookmarks: [],
    reactions: {},
    adFreeUntil: null,
    sensitivity: "Standard" as const,
    lastReadDate: null,
  };

  const savedArticles = mockArticles.filter((art) => currentPrefs.bookmarks.includes(art.id));

  return (
    <div className="relative w-full max-w-lg mx-auto min-h-screen bg-background text-white flex flex-col justify-between overflow-x-hidden font-sans border-x border-white/5 shadow-2xl">
      
      {/* Top Floating App Bar */}
      {currentScreen !== "Splash" && currentScreen !== "Onboarding" && currentScreen !== "Offline" && (
        <header className="sticky top-0 left-0 w-full flex justify-between items-center px-6 h-16 bg-surface-dim/95 backdrop-blur-md z-40 border-b border-white/5">
          <div className="flex items-center gap-3">
            <Zap className="text-primary h-5 w-5 fill-primary shrink-0 animate-pulse" />
            <h1 className="text-xl font-black tracking-tighter text-primary">TATTLE</h1>
          </div>

          <div className="flex items-center gap-3.5">
            {/* Ad free indicator */}
            {currentPrefs.adFreeUntil && (
              <span className="text-[9px] font-mono font-black bg-secondary/10 border border-secondary/20 text-secondary px-2.5 py-1 rounded-full uppercase tracking-wider">
                AD FREE
              </span>
            )}

            {/* Simulated Offline Toggle */}
            <button
              onClick={toggleOfflineSimulation}
              className={`p-2 rounded-full border transition-all cursor-pointer flex items-center justify-center ${
                isOffline
                  ? "bg-secondary/15 border-secondary text-secondary"
                  : "bg-white/5 border-white/5 text-outline hover:text-white"
              }`}
              title={isOffline ? "Go Online" : "Simulate Offline State"}
            >
              {isOffline ? <WifiOff className="h-4 w-4" /> : <Wifi className="h-4 w-4" />}
            </button>
          </div>
        </header>
      )}

      {/* Global Toast Notification */}
      <AnimatePresence>
        {shareToast && (
          <motion.div
            initial={{ opacity: 0, y: -50 }}
            animate={{ opacity: 1, y: 16 }}
            exit={{ opacity: 0, y: -50 }}
            className="fixed top-0 left-1/2 -translate-x-1/2 bg-secondary text-black font-semibold text-xs font-mono py-3 px-5 rounded-full shadow-2xl z-50 flex items-center gap-2"
          >
            <Check className="h-4 w-4 stroke-[2.5px]" />
            <span>{shareToast}</span>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Primary screens rendering */}
      <div className="flex-1 w-full flex flex-col relative">
        
        {currentScreen === "Splash" && (
          <Splash onGetStarted={() => setCurrentScreen("Onboarding")} />
        )}
        
        {currentScreen === "Onboarding" && (
          <Onboarding onComplete={handleCompleteOnboarding} />
        )}

        {currentScreen === "Feed" && preferences && (
          <Feed
            articles={mockArticles}
            preferences={currentPrefs}
            onUpdatePreferences={handleUpdatePreferences}
            onOpenArticle={(art) => setActiveArticle(art)}
            onLaunchBrief={(art) => setActiveBriefArticle(art)}
            isOffline={isOffline}
            onShareArticle={handleShareArticle}
          />
        )}

        {currentScreen === "Recap" && (
          <Recap
            preferences={currentPrefs}
            onTuneFeed={() => setCurrentScreen("Settings")}
          />
        )}

        {currentScreen === "Settings" && (
          <Settings
            preferences={currentPrefs}
            onUpdatePreferences={handleUpdatePreferences}
            onPurgeData={handlePurgeData}
          />
        )}

        {currentScreen === "Offline" && (
          <ErrorView
            savedArticles={savedArticles.length > 0 ? savedArticles : mockArticles.slice(1, 4)}
            onRetry={() => {
              setIsOffline(false);
              setCurrentScreen("Feed");
            }}
            onOpenArticle={(art) => setActiveArticle(art)}
            onToggleBookmark={(id) => {
              let bookmarks = [...currentPrefs.bookmarks];
              if (bookmarks.includes(id)) {
                bookmarks = bookmarks.filter((x) => x !== id);
              } else {
                bookmarks.push(id);
              }
              handleUpdatePreferences({ ...currentPrefs, bookmarks });
            }}
          />
        )}

      </div>

      {/* Bottom Floating Navigation Bar matching spec wireframes */}
      {currentScreen !== "Splash" && currentScreen !== "Onboarding" && currentScreen !== "Offline" && (
        <nav className="sticky bottom-0 left-0 w-full z-40 flex justify-around items-center px-6 pb-6 pt-2 bg-surface-dim/95 backdrop-blur-xl border-t border-white/5 rounded-t-2xl shadow-2xl">
          
          {/* Bolt: Feed view */}
          <button
            onClick={() => setCurrentScreen("Feed")}
            className={`flex flex-col items-center justify-center p-3 rounded-full transition-all duration-300 cursor-pointer ${
              currentScreen === "Feed"
                ? "text-primary bg-primary/10 scale-95"
                : "text-outline hover:text-white"
            }`}
          >
            <Zap className={`h-5 w-5 ${currentScreen === "Feed" ? "fill-primary" : ""}`} />
            <span className="text-[9px] font-mono font-extrabold tracking-wider mt-1 uppercase">
              FOR YOU
            </span>
          </button>

          {/* Compass: Brief popup explorer of top story */}
          <button
            onClick={() => {
              // Open brief for first item in dataset
              setActiveBriefArticle(mockArticles[0]);
            }}
            className="flex flex-col items-center justify-center p-3 rounded-full text-outline hover:text-white transition-all cursor-pointer"
          >
            <Compass className="h-5 w-5" />
            <span className="text-[9px] font-mono font-extrabold tracking-wider mt-1 uppercase">
              EXPLORE
            </span>
          </button>

          {/* Bar chart: Recap screen */}
          <button
            onClick={() => setCurrentScreen("Recap")}
            className={`flex flex-col items-center justify-center p-3 rounded-full transition-all duration-300 cursor-pointer ${
              currentScreen === "Recap"
                ? "text-primary bg-primary/10 scale-95"
                : "text-outline hover:text-white"
            }`}
          >
            <BarChart3 className="h-5 w-5" />
            <span className="text-[9px] font-mono font-extrabold tracking-wider mt-1 uppercase">
              TRENDS
            </span>
          </button>

          {/* Settings gear option */}
          <button
            onClick={() => setCurrentScreen("Settings")}
            className={`flex flex-col items-center justify-center p-3 rounded-full transition-all duration-300 cursor-pointer ${
              currentScreen === "Settings"
                ? "text-primary bg-primary/10 scale-95"
                : "text-outline hover:text-white"
            }`}
          >
            <SettingsIcon className="h-5 w-5" />
            <span className="text-[9px] font-mono font-extrabold tracking-wider mt-1 uppercase">
              SAVED
            </span>
          </button>

        </nav>
      )}

      {/* DISCLOSURE MODAL OVERLAYS */}
      
      {/* 1. Brief Card View overlay */}
      <AnimatePresence>
        {activeBriefArticle && (
          <BriefView
            article={activeBriefArticle}
            isBookmarked={currentPrefs.bookmarks.includes(activeBriefArticle.id)}
            onClose={() => setActiveBriefArticle(null)}
            onToggleBookmark={() => {
              let bookmarks = [...currentPrefs.bookmarks];
              if (bookmarks.includes(activeBriefArticle.id)) {
                bookmarks = bookmarks.filter((x) => x !== activeBriefArticle.id);
              } else {
                bookmarks.push(activeBriefArticle.id);
              }
              handleUpdatePreferences({ ...currentPrefs, bookmarks });
            }}
            onShare={() => handleShareArticle(activeBriefArticle)}
            onLaunchFullText={() => {
              setActiveArticle(activeBriefArticle);
              setActiveBriefArticle(null);
            }}
          />
        )}
      </AnimatePresence>

      {/* 2. Full Article View overlay */}
      <AnimatePresence>
        {activeArticle && (
          <FullView
            article={activeArticle}
            isBookmarked={currentPrefs.bookmarks.includes(activeArticle.id)}
            onClose={() => setActiveArticle(null)}
            onToggleBookmark={() => {
              let bookmarks = [...currentPrefs.bookmarks];
              if (bookmarks.includes(activeArticle.id)) {
                bookmarks = bookmarks.filter((x) => x !== activeArticle.id);
              } else {
                bookmarks.push(activeArticle.id);
              }
              handleUpdatePreferences({ ...currentPrefs, bookmarks });
            }}
            onShare={() => handleShareArticle(activeArticle)}
            currentReaction={currentPrefs.reactions[activeArticle.id]}
            onReaction={(emoji) => {
              const reactions = { ...currentPrefs.reactions, [activeArticle.id]: emoji };
              handleUpdatePreferences({ ...currentPrefs, reactions });
            }}
            nextArticle={
              mockArticles[
                (mockArticles.findIndex((x) => x.id === activeArticle.id) + 1) % mockArticles.length
              ]
            }
            onLoadNextArticle={(nextArt) => setActiveArticle(nextArt)}
          />
        )}
      </AnimatePresence>

      {/* 3. Social Media Instagram/TikTok Export Template Asset Preview */}
      <AnimatePresence>
        {showShareAsset && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-background/90 backdrop-blur-md z-50 flex flex-col justify-center items-center p-6"
          >
            <div className="absolute inset-0 z-0" onClick={() => setShowShareAsset(null)} />
            
            {/* Visual share overlay container */}
            <div className="relative z-10 w-full max-w-xs bg-surface rounded-3xl p-6 border border-white/5 space-y-6 shadow-2xl overflow-hidden flex flex-col">
              
              {/* Instagram/TikTok Header indicator */}
              <div className="text-center">
                <span className="text-[10px] font-mono font-bold text-outline uppercase tracking-widest">
                  EXPORT ASSET: STORIES / TIKTOK
                </span>
              </div>

              {/* The mobile visual mock render */}
              <div className="relative aspect-[9/16] w-full rounded-2xl overflow-hidden border border-white/10 shadow-xl bg-background p-4 flex flex-col justify-between">
                {/* Background cover grayscale image */}
                <img
                  src={showShareAsset.imageUrl}
                  alt={showShareAsset.headline}
                  className="absolute inset-0 w-full h-full object-cover grayscale brightness-40 pointer-events-none"
                />
                
                {/* Visual gradient overlay */}
                <div className="absolute inset-0 bg-gradient-to-t from-background via-transparent to-black/20 pointer-events-none" />

                {/* Content */}
                <div className="relative z-10 flex items-center gap-1.5 pt-2">
                  {/* Small branding */}
                  <div className="w-5 h-5 rounded bg-primary p-0.5 flex items-center justify-center font-extrabold text-black text-[10px]">
                    T
                  </div>
                  <span className="text-xs font-black tracking-widest text-white">TATTLE</span>
                </div>

                <div className="relative z-10 space-y-3 pb-4">
                  <div className="h-1 w-12 bg-primary rounded-full" />
                  <h3 className="text-xl font-extrabold text-white leading-tight">
                    {showShareAsset.headline.length > 50
                      ? showShareAsset.headline.substring(0, 50) + "..."
                      : showShareAsset.headline}
                  </h3>
                  <p className="text-[11px] text-text-muted leading-relaxed line-clamp-2">
                    {showShareAsset.hook}
                  </p>
                </div>

                {/* Bottom slide action indicator overlay */}
                <div className="relative z-10 text-center space-y-1">
                  <div className="w-1.5 h-1.5 rounded-full bg-primary mx-auto animate-bounce" />
                  <span className="text-[8px] font-mono font-extrabold tracking-widest text-outline uppercase">
                    SWIPE FOR UPDATE
                  </span>
                </div>
              </div>

              {/* Actions row */}
              <div className="space-y-2 relative z-10">
                <button
                  onClick={handleTriggerShareCopy}
                  className="w-full h-12 bg-primary hover:bg-primary-light text-black font-semibold text-xs tracking-widest uppercase rounded-full flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-primary/10 transition-transform active:scale-98"
                >
                  <Share2 className="h-4 w-4 stroke-[2.5px]" />
                  <span>Copy Share Asset Link</span>
                </button>
                <button
                  onClick={() => setShowShareAsset(null)}
                  className="w-full text-center text-xs font-semibold text-outline hover:text-white transition-colors py-2 cursor-pointer"
                >
                  Cancel
                </button>
              </div>

            </div>
          </motion.div>
        )}
      </AnimatePresence>

    </div>
  );
}

