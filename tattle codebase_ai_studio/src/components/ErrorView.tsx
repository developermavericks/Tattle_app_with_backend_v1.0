/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from "react";
import { Article } from "../types";
import { WifiOff, AlertTriangle, RefreshCw, Bookmark, ArrowRight, Sparkles, BookOpen } from "lucide-react";
import { motion } from "motion/react";

interface ErrorViewProps {
  savedArticles: Article[];
  onRetry: () => void;
  onOpenArticle: (article: Article) => void;
  onToggleBookmark: (id: string) => void;
}

export const ErrorView: React.FC<ErrorViewProps> = ({
  savedArticles,
  onRetry,
  onOpenArticle,
  onToggleBookmark
}) => {
  const [viewMode, setViewMode] = useState<"error" | "saved">("error");
  const [isRetrying, setIsRetrying] = useState<boolean>(false);

  const handleRetryClick = () => {
    setIsRetrying(true);
    setTimeout(() => {
      setIsRetrying(false);
      onRetry();
    }, 1500);
  };

  return (
    <div className="w-full min-h-screen bg-background text-white select-none font-sans flex flex-col justify-between">
      
      {/* Persistent global top bar */}
      <header className="fixed top-0 left-0 w-full flex justify-between items-center px-6 h-16 bg-surface-dim z-50">
        <div className="flex items-center gap-2">
          {/* cellular icon */}
          <WifiOff className="text-primary h-5 w-5" />
          <span className="text-xl font-black tracking-tighter text-primary">TATTLE</span>
        </div>
        
        {/* Toggle between error state and cached list for demonstrative richness */}
        <div className="flex bg-white/5 p-1 rounded-lg border border-white/5">
          <button
            onClick={() => setViewMode("error")}
            className={`px-2 py-1 text-[9px] font-mono font-bold tracking-widest uppercase rounded ${
              viewMode === "error" ? "bg-primary text-black" : "text-outline"
            }`}
          >
            Error
          </button>
          <button
            onClick={() => setViewMode("saved")}
            className={`px-2 py-1 text-[9px] font-mono font-bold tracking-widest uppercase rounded ${
              viewMode === "saved" ? "bg-primary text-black" : "text-outline"
            }`}
          >
            Cached
          </button>
        </div>
      </header>

      {/* Main Container Content */}
      <main className="flex-grow flex flex-col justify-center pt-20 px-6 pb-24 max-w-md mx-auto w-full">
        
        {/* VIEW 1: TRANSMISSION HALTED (Image 1) */}
        {viewMode === "error" && (
          <div className="w-full flex flex-col items-center justify-center text-center space-y-6 py-6">
            
            {/* Ambient Background Blur */}
            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-64 h-64 bg-primary/10 rounded-full blur-[120px] pointer-events-none" />
            
            <div className="bg-surface rounded-3xl p-8 border border-white/5 flex flex-col items-center space-y-5 shadow-2xl relative">
              {/* Glitchy Bolt Icon */}
              <div className="relative mb-4">
                <div className="absolute -inset-4 bg-primary/10 blur-xl rounded-full" />
                <AlertTriangle className="h-16 w-16 text-secondary relative z-10 animate-bounce" />
              </div>

              <div className="space-y-1">
                <h1 className="text-2xl font-extrabold text-white leading-tight">
                  Transmission Halted
                </h1>
                <p className="text-xs text-text-muted max-w-[260px] mx-auto leading-relaxed">
                  We've hit a digital dead zone. The feed is currently unresponsive.
                </p>
              </div>

              {/* Technical Code pill */}
              <div className="bg-[#2C353F] px-4 py-1.5 rounded-full border border-white/5">
                <span className="text-[10px] font-mono font-bold text-outline uppercase tracking-widest">
                  Error Code: 503_FEED_DROP
                </span>
              </div>

              {/* Primary action */}
              <button
                onClick={handleRetryClick}
                disabled={isRetrying}
                className="w-full h-14 bg-primary hover:bg-primary-light text-black font-semibold text-xs tracking-widest uppercase rounded-full flex items-center justify-center gap-2 shadow-lg shadow-primary/20 cursor-pointer transition-transform active:scale-98"
              >
                <RefreshCw className={`h-4 w-4 ${isRetrying ? "animate-spin" : ""}`} />
                <span>{isRetrying ? "Reconnecting..." : "Retry Feed"}</span>
              </button>

              {/* Ghost option */}
              <button
                onClick={() => setViewMode("saved")}
                className="text-xs text-outline hover:text-white font-mono font-bold uppercase tracking-widest py-1 transition-colors cursor-pointer"
              >
                Browse Cached Stories
              </button>
            </div>
          </div>
        )}

        {/* VIEW 2: OFFLINE SAVED FEED (Image 2) */}
        {viewMode === "saved" && (
          <div className="w-full space-y-6 pt-4">
            
            {/* Top persistent warning banner */}
            <div className="fixed top-16 left-0 w-full h-10 bg-[#1F2833]/90 backdrop-blur-md border-b border-white/5 flex items-center justify-center gap-2 z-40 text-text-muted">
              <WifiOff className="h-4 w-4 text-outline" />
              <span className="text-xs font-semibold tracking-wide">
                You're offline — showing saved stories.
              </span>
            </div>

            {/* Headline section */}
            <div className="flex items-center justify-between pt-10">
              <h2 className="text-2xl font-extrabold text-white tracking-tight">
                Saved Stories
              </h2>
              <span className="bg-white/5 border border-white/5 px-2.5 py-1 rounded text-[10px] font-mono font-bold text-outline uppercase tracking-wider">
                CACHED 12M AGO
              </span>
            </div>

            {/* List of articles */}
            {savedArticles.length === 0 ? (
              <div className="glass-panel p-8 rounded-2xl border border-white/5 text-center space-y-3">
                <BookOpen className="h-10 w-10 text-outline mx-auto" />
                <p className="text-xs text-text-muted font-medium">
                  No cached stories available. Bookmark articles during live sessions to access them offline!
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {savedArticles.map((article) => (
                  <div
                    key={article.id}
                    className="glass-panel rounded-2xl overflow-hidden border border-white/5 hover:border-white/10 transition-all flex flex-col"
                  >
                    {/* Optional Image */}
                    <div className="aspect-video w-full relative">
                      <img
                        src={article.imageUrl}
                        alt={article.headline}
                        className="w-full h-full object-cover grayscale opacity-60"
                      />
                      <div className="absolute top-3 left-3">
                        <span className="bg-secondary-container text-on-secondary-container px-2 py-0.5 rounded text-[9px] font-mono font-bold tracking-wider uppercase">
                          #{article.category}
                        </span>
                      </div>
                    </div>

                    <div className="p-4 space-y-3">
                      <h3
                        onClick={() => onOpenArticle(article)}
                        className="text-base font-extrabold text-white leading-snug cursor-pointer hover:text-primary transition-colors"
                      >
                        {article.headline}
                      </h3>

                      <div className="flex items-center justify-between pt-2 border-t border-white/5 text-[10px] font-mono text-outline font-bold">
                        <div className="flex items-center gap-2">
                          <span className="text-primary">{article.publisher}</span>
                          <span>•</span>
                          <span>{article.readTime}</span>
                        </div>
                        
                        <button
                          onClick={() => onToggleBookmark(article.id)}
                          className="text-secondary p-1"
                        >
                          <Bookmark className="h-4 w-4 fill-secondary" />
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Reconnect footer banner */}
            <div className="flex flex-col items-center text-center space-y-3 pt-8 pb-4">
              <WifiOff className="h-8 w-8 text-outline opacity-40" />
              <p className="text-xs text-text-muted font-medium leading-relaxed max-w-[200px]">
                Reconnect to see the latest breaking gossip.
              </p>
              
              <button
                onClick={handleRetryClick}
                disabled={isRetrying}
                className="border border-outline/30 hover:border-white text-xs font-mono font-bold uppercase tracking-widest px-6 h-11 rounded-full text-outline hover:text-white transition-all cursor-pointer flex items-center justify-center gap-2 active:scale-98"
              >
                <RefreshCw className={`h-3.5 w-3.5 ${isRetrying ? "animate-spin" : ""}`} />
                <span>{isRetrying ? "Reconnecting..." : "Retry Connection"}</span>
              </button>
            </div>

          </div>
        )}

      </main>
    </div>
  );
};
