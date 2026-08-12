/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { Article, UserPreferences, Survey } from "../types";
import { mockSurveys } from "../data/news";
import { SurveyCard } from "./SurveyCard";
import { Bookmark, Share2, Eye, Flame, Clock, RefreshCw, Sparkles, MessageSquare, Heart, ThumbsDown } from "lucide-react";
import { motion, useMotionValue, useTransform, useAnimation } from "motion/react";

interface FeedProps {
  articles: Article[];
  preferences: UserPreferences;
  onUpdatePreferences: (prefs: UserPreferences) => void;
  onOpenArticle: (article: Article) => void;
  onLaunchBrief: (article: Article) => void;
  isOffline: boolean;
  onShareArticle: (article: Article) => void;
}

export const Feed: React.FC<FeedProps> = ({
  articles,
  preferences,
  onUpdatePreferences,
  onOpenArticle,
  onLaunchBrief,
  isOffline,
  onShareArticle
}) => {
  const [activeTab, setActiveTab] = useState<string>("For You");
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [showReactionsForId, setShowReactionsForId] = useState<string | null>(null);
  const [surveyCompleted, setSurveyCompleted] = useState<boolean>(false);
  const [showSurvey, setShowSurvey] = useState<boolean>(false);
  const [cardsSinceSurvey, setCardsSinceSurvey] = useState<number>(0);

  // Gesture motion values for the active top card
  const dragX = useMotionValue(0);
  const dragY = useMotionValue(0);
  const rotate = useTransform(dragX, [-200, 200], [-15, 15]);
  const opacity = useTransform(dragX, [-200, -150, 0, 150, 200], [0.5, 0.8, 1, 0.8, 0.5]);
  const tiltX = useTransform(dragX, [-200, 200], [-20, 20]);

  // Transform overlays for swipe signals
  const likeOpacity = useTransform(dragX, [0, 120], [0, 1]);
  const dismissOpacity = useTransform(dragX, [-120, 0], [1, 0]);

  const controls = useAnimation();

  // Filter articles based on active tab
  const getFilteredArticles = () => {
    if (activeTab === "Saved") {
      return articles.filter((a) => preferences.bookmarks.includes(a.id));
    }
    if (activeTab === "For You") {
      // Prioritize interests
      return articles.filter((a) => {
        // Safe check for match
        return true;
      });
    }
    return articles.filter((a) => a.category.toLowerCase().includes(activeTab.toLowerCase()));
  };

  const filteredList = getFilteredArticles();
  const currentArticle = filteredList[currentIndex];

  const handleSwipeRight = () => {
    if (!currentArticle) return;
    
    // Add to read history and increment totals
    const history = [...preferences.readHistory];
    if (!history.includes(currentArticle.id)) {
      history.push(currentArticle.id);
    }

    onUpdatePreferences({
      ...preferences,
      readHistory: history,
      totalCardsRead: preferences.totalCardsRead + 1
    });

    proceedNext();
  };

  const handleSwipeLeft = () => {
    proceedNext();
  };

  const proceedNext = () => {
    // Increment cards consumed towards next survey checkpoint (e.g. every 4 cards)
    const newCardsCount = cardsSinceSurvey + 1;
    setCardsSinceSurvey(newCardsCount);

    if (newCardsCount >= 4 && !preferences.adFreeUntil) {
      setShowSurvey(true);
      setCardsSinceSurvey(0);
    } else {
      if (currentIndex < filteredList.length - 1) {
        setCurrentIndex(currentIndex + 1);
      } else {
        // Loop back or show recap
        setCurrentIndex(0);
      }
    }
    
    // Reset drag value positions
    dragX.set(0);
    dragY.set(0);
  };

  // Drag End Gesture Callback
  const handleDragEnd = (event: any, info: any) => {
    const threshold = 120;
    if (info.offset.x > threshold) {
      // Swipe Right (Interested)
      controls.start({ x: 500, opacity: 0 }).then(() => {
        handleSwipeRight();
        controls.set({ x: 0, opacity: 1 });
      });
    } else if (info.offset.x < -threshold) {
      // Swipe Left (Dismiss)
      controls.start({ x: -500, opacity: 0 }).then(() => {
        handleSwipeLeft();
        controls.set({ x: 0, opacity: 1 });
      });
    } else {
      // Snap Back
      controls.start({ x: 0, y: 0 });
    }
  };

  // Toggle bookmark / save
  const handleToggleBookmark = (id: string, e: React.MouseEvent) => {
    e.stopPropagation();
    let bookmarks = [...preferences.bookmarks];
    if (bookmarks.includes(id)) {
      bookmarks = bookmarks.filter((x) => x !== id);
    } else {
      bookmarks.push(id);
    }
    onUpdatePreferences({ ...preferences, bookmarks });
  };

  // Emoji reaction triggers
  const handleApplyReaction = (articleId: string, emoji: string, e: React.MouseEvent) => {
    e.stopPropagation();
    const reactions = { ...preferences.reactions, [articleId]: emoji };
    onUpdatePreferences({ ...preferences, reactions });
    setShowReactionsForId(null);
  };

  const handleDoubleTap = (articleId: string) => {
    setShowReactionsForId(articleId);
  };

  // Clear survey checkpoint and reward ad-free time (3 hours)
  const handleSurveyComplete = () => {
    const expiration = new Date();
    expiration.setHours(expiration.getHours() + 3);

    onUpdatePreferences({
      ...preferences,
      adFreeUntil: expiration.toISOString()
    });

    setSurveyCompleted(true);
    setTimeout(() => {
      setShowSurvey(false);
      setSurveyCompleted(false);
      proceedNext();
    }, 1000);
  };

  const tabsList = ["For You", "Tech", "Culture", "Politics", "Saved"];

  return (
    <div className="w-full flex flex-col font-sans select-none relative h-full">
      
      {/* Top filter tabs list */}
      <div className="sticky top-0 left-0 w-full bg-background/95 backdrop-blur-md z-30 pt-4 pb-2 shrink-0 border-b border-white/5">
        <div className="flex gap-2.5 overflow-x-auto px-6 no-scrollbar">
          {tabsList.map((tab) => {
            const isActive = activeTab === tab;
            return (
              <button
                key={tab}
                onClick={() => {
                  setActiveTab(tab);
                  setCurrentIndex(0);
                }}
                className={`px-4.5 py-2 rounded-full font-bold text-xs tracking-wide uppercase transition-all duration-300 shrink-0 cursor-pointer ${
                  isActive
                    ? "bg-primary text-black shadow-lg shadow-primary/10"
                    : "bg-surface-container/40 text-text-muted hover:text-white"
                }`}
              >
                {tab}
              </button>
            );
          })}
        </div>
      </div>

      {/* Progress tracking thin cyan line at the top of main deck view */}
      <div className="w-full h-[2px] bg-white/5 relative z-10 shrink-0">
        <div
          className="h-full bg-primary shadow-[0_0_8px_#45A29E] transition-all duration-500"
          style={{
            width: `${((currentIndex + 1) / Math.max(filteredList.length, 1)) * 100}%`
          }}
        />
      </div>

      {/* Main deck area */}
      <div className="flex-1 flex items-center justify-center px-6 py-6 overflow-hidden relative min-h-[460px]">
        
        {/* Offline persistent notice banner */}
        {isOffline && (
          <div className="absolute top-2 left-6 right-6 bg-[#1F2833]/90 border border-white/5 text-xs text-text-muted px-4 py-2 rounded-full flex items-center justify-center gap-2 z-40">
            <span className="w-1.5 h-1.5 bg-outline rounded-full" />
            <span>You're offline — showing saved cached stories.</span>
          </div>
        )}

        {/* SURVEY MONETIZATION INJECTION VIEW */}
        {showSurvey ? (
          <div className="w-full max-w-sm">
            <SurveyCard
              survey={mockSurveys[0]}
              onComplete={handleSurveyComplete}
              onSkip={() => {
                setShowSurvey(false);
                proceedNext();
              }}
              onWatchAd={() => {
                alert("Simulating standard video ad playback... ad finished!");
                handleSurveyComplete();
              }}
            />
          </div>
        ) : !currentArticle ? (
          /* Empty/No story state */
          <div className="text-center p-8 space-y-4 max-w-xs">
            <Sparkles className="h-10 w-10 text-outline mx-auto animate-pulse" />
            <h3 className="text-lg font-extrabold text-white">Deck Empty</h3>
            <p className="text-xs text-text-muted leading-relaxed">
              {activeTab === "Saved"
                ? "You haven't bookmarked any articles yet. Bookmark them on the feed to access them later."
                : "Come back later — new stories drop throughout the day."}
            </p>
            {activeTab === "Saved" && (
              <button
                onClick={() => setActiveTab("For You")}
                className="text-xs text-primary font-bold tracking-widest font-mono uppercase cursor-pointer"
              >
                Browse Feed
              </button>
            )}
          </div>
        ) : (
          /* GESTURE CARD DECK VIEW */
          <div className="relative w-full max-w-sm aspect-[4/5] min-h-[400px]">
            <motion.div
              style={{ x: dragX, y: dragY, rotate, opacity }}
              drag
              dragConstraints={{ left: 0, right: 0, top: 0, bottom: 0 }}
              dragElastic={0.6}
              onDragEnd={handleDragEnd}
              animate={controls}
              onDoubleClick={() => handleDoubleTap(currentArticle.id)}
              className="absolute inset-0 bg-[#1F2833] rounded-3xl border border-white/5 overflow-hidden flex flex-col justify-between p-5 cursor-grab active:cursor-grabbing shadow-2xl relative z-20"
            >
              
              {/* Swipe directional text overlays */}
              <motion.div
                style={{ opacity: likeOpacity }}
                className="absolute top-8 left-8 border-3 border-primary text-primary font-mono font-extrabold text-sm tracking-widest px-4 py-1.5 rounded-lg rotate-[-12deg] uppercase z-30 bg-background/80 backdrop-blur-sm shadow-xl"
              >
                INTERESTED
              </motion.div>
              
              <motion.div
                style={{ opacity: dismissOpacity }}
                className="absolute top-8 right-8 border-3 border-secondary text-secondary font-mono font-extrabold text-sm tracking-widest px-4 py-1.5 rounded-lg rotate-[12deg] uppercase z-30 bg-background/80 backdrop-blur-sm shadow-xl"
              >
                DISMISS
              </motion.div>

              {/* Cover Image & Category layout */}
              <div className="aspect-[4/3] rounded-2xl overflow-hidden relative border border-white/5 shrink-0 bg-background">
                <img
                  src={currentArticle.imageUrl}
                  alt={currentArticle.headline}
                  className="w-full h-full object-cover grayscale opacity-70 pointer-events-none"
                />
                
                {/* Category & read details */}
                <div className="absolute top-3 left-3 flex gap-2">
                  {currentArticle.isBreaking && (
                    <span className="bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-[9px] font-mono font-extrabold tracking-widest uppercase shadow-md">
                      BREAKING
                    </span>
                  )}
                  <span className="bg-primary/20 backdrop-blur-md text-primary border border-primary/20 px-2 py-0.5 rounded text-[9px] font-mono font-bold tracking-wider uppercase">
                    #{currentArticle.category}
                  </span>
                </div>

                <div className="absolute bottom-3 right-3 bg-background/80 backdrop-blur-md px-2 py-0.5 rounded text-[9px] font-mono text-outline font-bold">
                  {currentArticle.readTime}
                </div>
              </div>

              {/* Title & Hook description */}
              <div className="flex-1 flex flex-col justify-center py-3 space-y-1.5">
                <h3
                  onClick={() => onLaunchBrief(currentArticle)}
                  className="text-[17px] font-black text-white leading-tight font-sans tracking-wide cursor-pointer hover:text-primary transition-colors line-clamp-2"
                >
                  {currentArticle.headline}
                </h3>
                
                <p className="text-xs text-text-muted leading-relaxed line-clamp-2 font-medium">
                  {currentArticle.hook}
                </p>
              </div>

              {/* Publisher & Share Footer bar */}
              <div className="flex items-center justify-between border-t border-white/5 pt-3 mt-1 shrink-0">
                <div className="flex items-center gap-2">
                  <div className="w-5 h-5 bg-primary/20 text-primary font-bold text-[10px] rounded-full flex items-center justify-center font-mono">
                    T
                  </div>
                  <div className="flex flex-col">
                    <span className="text-[10px] font-extrabold text-white leading-none">
                      {currentArticle.publisher}
                    </span>
                    <span className="text-[8px] font-mono text-outline leading-none mt-0.5">
                      {currentArticle.publishedAt}
                    </span>
                  </div>
                </div>

                {/* Hand Action controls */}
                <div className="flex items-center gap-1.5">
                  {preferences.reactions[currentArticle.id] && (
                    <span className="text-sm px-1.5 py-0.5 bg-white/5 rounded-full border border-white/5">
                      {preferences.reactions[currentArticle.id]}
                    </span>
                  )}

                  <button
                    onClick={(e) => handleToggleBookmark(currentArticle.id, e)}
                    className="p-2 text-outline hover:text-white rounded-full hover:bg-white/5 transition-colors cursor-pointer"
                  >
                    <Bookmark
                      className={`h-4.5 w-4.5 ${
                        preferences.bookmarks.includes(currentArticle.id) ? "text-secondary fill-secondary" : ""
                      }`}
                    />
                  </button>

                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      onShareArticle(currentArticle);
                    }}
                    className="p-2 text-outline hover:text-white rounded-full hover:bg-white/5 transition-colors cursor-pointer"
                  >
                    <Share2 className="h-4.5 w-4.5" />
                  </button>
                </div>
              </div>

              {/* Double tap emoji reactions pop up overlay */}
              {showReactionsForId === currentArticle.id && (
                <div className="absolute inset-x-4 bottom-16 bg-background/95 backdrop-blur-md border border-white/10 rounded-2xl p-3 flex justify-around shadow-2xl z-40 animate-fade-in">
                  {["❤️", "😂", "😮", "🤯", "😡"].map((emoji) => (
                    <button
                      key={emoji}
                      onClick={(e) => handleApplyReaction(currentArticle.id, emoji, e)}
                      className="text-xl hover:scale-125 transition-transform duration-100 cursor-pointer p-1"
                    >
                      {emoji}
                    </button>
                  ))}
                </div>
              )}
            </motion.div>

            {/* Back Stack Card (Visual layout balance for deck thickness) */}
            {filteredList[currentIndex + 1] && (
              <div className="absolute inset-0 bg-[#1F2833]/60 rounded-3xl border border-white/5 scale-[0.96] translate-y-3 -z-10 shadow-lg" />
            )}
            {filteredList[currentIndex + 2] && (
              <div className="absolute inset-0 bg-[#1F2833]/30 rounded-3xl border border-white/5 scale-[0.92] translate-y-6 -z-20 shadow-md" />
            )}
          </div>
        )}

      </div>

      {/* Primary manual hand swipe fallbacks shown in Bite wireframe */}
      {currentArticle && !showSurvey && (
        <div className="w-full max-w-sm mx-auto px-6 pb-6 pt-2 shrink-0 flex items-center justify-around gap-4 z-10">
          <button
            onClick={() => {
              controls.start({ x: -300, opacity: 0 }).then(() => {
                handleSwipeLeft();
                controls.set({ x: 0, opacity: 1 });
              });
            }}
            className="flex-1 py-3 bg-[#111415] hover:bg-surface border border-white/5 rounded-full font-mono font-extrabold text-[11px] tracking-widest text-outline hover:text-white uppercase text-center transition-all cursor-pointer flex items-center justify-center gap-1 shadow-md active:scale-95"
          >
            <ThumbsDown className="h-3 w-3 shrink-0" />
            <span>DISMISS</span>
          </button>
          
          <button
            onClick={() => onLaunchBrief(currentArticle)}
            className="flex-1 py-3 bg-primary hover:bg-primary-light rounded-full font-mono font-extrabold text-[11px] tracking-widest text-black uppercase text-center transition-all cursor-pointer flex items-center justify-center gap-1 shadow-lg shadow-primary/10 active:scale-95"
          >
            <Sparkles className="h-3 w-3 shrink-0 fill-black" />
            <span>EXPLORE</span>
          </button>
        </div>
      )}
    </div>
  );
};
