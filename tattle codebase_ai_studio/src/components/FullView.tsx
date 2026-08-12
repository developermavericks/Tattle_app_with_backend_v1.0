/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useState, useRef } from "react";
import { Article } from "../types";
import { ArrowLeft, Share2, Bookmark, Flame, MessageSquare, Plus, Check } from "lucide-react";
import { motion } from "motion/react";

interface FullViewProps {
  article: Article;
  isBookmarked: boolean;
  onClose: () => void;
  onToggleBookmark: () => void;
  onShare: () => void;
  onReaction?: (emoji: string) => void;
  currentReaction?: string;
  nextArticle?: Article;
  onLoadNextArticle?: (nextArt: Article) => void;
}

export const FullView: React.FC<FullViewProps> = ({
  article,
  isBookmarked,
  onClose,
  onToggleBookmark,
  onShare,
  onReaction,
  currentReaction,
  nextArticle,
  onLoadNextArticle
}) => {
  const containerRef = useRef<HTMLDivElement>(null);
  const [scrollProgress, setScrollProgress] = useState<number>(0);
  const [isFollowingThread, setIsFollowingThread] = useState<boolean>(false);

  // Track reading scroll progress
  const handleScroll = () => {
    const element = containerRef.current;
    if (!element) return;
    const totalHeight = element.scrollHeight - element.clientHeight;
    if (totalHeight > 0) {
      setScrollProgress((element.scrollTop / totalHeight) * 100);
    }
  };

  useEffect(() => {
    const element = containerRef.current;
    if (element) {
      element.addEventListener("scroll", handleScroll);
    }
    return () => {
      if (element) {
        element.removeEventListener("scroll", handleScroll);
      }
    };
  }, [article]);

  return (
    <motion.div
      initial={{ opacity: 0, x: "100%" }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: "100%" }}
      transition={{ type: "spring", damping: 30, stiffness: 280 }}
      className="fixed inset-0 bg-background z-50 flex flex-col"
    >
      {/* Top sticky bar with reader reading progress */}
      <header className="sticky top-0 left-0 w-full bg-background/95 backdrop-blur-md px-4 h-16 flex items-center justify-between border-b border-white/5 z-20">
        <div className="flex items-center gap-3">
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-surface-variant text-outline hover:text-white transition-colors cursor-pointer"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          
          <div className="flex flex-col">
            <span className="text-[10px] font-mono font-extrabold text-outline uppercase tracking-widest">
              TATTLE EXCLUSIVE
            </span>
            <span className="text-xs font-bold text-white truncate max-w-[200px]">
              {article.headline}
            </span>
          </div>
        </div>

        <div className="flex items-center gap-1.5">
          <button
            onClick={onShare}
            className="p-2.5 rounded-full hover:bg-surface-variant text-outline hover:text-white transition-colors cursor-pointer"
          >
            <Share2 className="h-4 w-4" />
          </button>
          
          <button
            onClick={onToggleBookmark}
            className={`p-2.5 rounded-full hover:bg-surface-variant transition-colors cursor-pointer ${
              isBookmarked ? "text-secondary" : "text-outline hover:text-white"
            }`}
          >
            <Bookmark className={`h-4 w-4 ${isBookmarked ? "fill-secondary" : ""}`} />
          </button>
        </div>

        {/* 2px Cyan Reading progress bar */}
        <div className="absolute bottom-0 left-0 h-[2px] bg-white/5 w-full">
          <div
            className="h-full bg-primary shadow-[0_0_8px_#45A29E] transition-all duration-100"
            style={{ width: `${scrollProgress}%` }}
          />
        </div>
      </header>

      {/* Main scrollable layout container */}
      <div
        ref={containerRef}
        className="flex-1 overflow-y-auto pb-32 no-scrollbar"
      >
        <div className="max-w-xl mx-auto px-6 py-6 space-y-6">
          
          {/* Cover image & labels */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <span className="text-[10px] font-mono font-extrabold text-secondary uppercase bg-secondary/10 px-2 py-0.5 rounded tracking-widest">
                #{article.category.toUpperCase()}
              </span>
              <span className="text-outline text-[10px] font-mono font-bold uppercase tracking-wider">
                • {article.readTime}
              </span>
            </div>

            <h1 className="text-3xl font-extrabold text-white leading-tight font-sans">
              {article.headline}
            </h1>

            {/* Author Credit with profile badge */}
            <div className="flex items-center gap-3 py-2 border-y border-white/5">
              <div className="w-10 h-10 rounded-full bg-surface-variant overflow-hidden border border-white/10 flex items-center justify-center font-extrabold text-primary font-mono shrink-0">
                AT
              </div>
              <div>
                <p className="text-sm font-bold text-white">Alex Thorne</p>
                <p className="text-[10px] font-mono font-bold text-outline uppercase tracking-wider">
                  MAR 14, 2026 • TOKYO HUB
                </p>
              </div>
            </div>
          </div>

          {/* Hero cover image */}
          <div className="relative aspect-video w-full rounded-2xl overflow-hidden border border-white/5 shadow-2xl">
            <img
              src={article.imageUrl}
              alt={article.headline}
              className="w-full h-full object-cover"
            />
          </div>

          {/* Core Body Rich-Text Copy */}
          <article className="prose prose-invert max-w-none text-text-muted text-sm leading-relaxed space-y-5 font-sans font-medium">
            {article.fullText.split("\n\n").map((para, idx) => {
              // Inject a pull-quote on paragraph index 2 as shown in visual spec
              if (idx === 2) {
                return (
                  <div key={idx} className="space-y-5">
                    <p>{para}</p>
                    
                    {/* Cyber blockquote pull quote */}
                    <div className="neon-border bg-primary/5 p-6 rounded-2xl relative border-l-4 border-l-primary my-6">
                      <p className="text-lg font-extrabold italic text-primary leading-normal font-sans">
                        "The noise is the message. The speed is the verification. If you're not scrolling, you're becoming obsolete."
                      </p>
                    </div>
                  </div>
                );
              }
              return <p key={idx}>{para}</p>;
            })}
          </article>

          {/* Sub tags footer */}
          <div className="flex flex-wrap gap-2 pt-4 border-t border-white/5">
            {["#cybernetics", "#media-theory", "#gen-z", "#breaking"].map((tag) => (
              <span
                key={tag}
                className="text-xs font-mono font-bold text-text-muted bg-surface px-3 py-1.5 rounded-full border border-white/5 hover:border-white/15 transition-colors"
              >
                {tag}
              </span>
            ))}
          </div>

          {/* Interaction stats and reaction emojis row */}
          <div className="glass-panel p-4 rounded-2xl flex items-center justify-between border border-white/5 mt-6">
            <div className="flex items-center gap-4 text-xs font-mono text-outline">
              <span className="flex items-center gap-1.5">
                <Flame className="h-4 w-4 text-secondary fill-secondary" />
                {article.likes}
              </span>
              <span className="flex items-center gap-1.5">
                <MessageSquare className="h-4 w-4" />
                {article.commentsCount}
              </span>
            </div>

            {/* Follow Thread button */}
            <button
              onClick={() => setIsFollowingThread(!isFollowingThread)}
              className={`px-4 h-10 rounded-full font-mono font-bold text-[11px] tracking-wider uppercase transition-all duration-200 flex items-center gap-1.5 cursor-pointer ${
                isFollowingThread
                  ? "bg-secondary text-black"
                  : "bg-primary/20 text-primary border border-primary/30 hover:bg-primary/35"
              }`}
            >
              {isFollowingThread ? (
                <>
                  <Check className="h-3.5 w-3.5 stroke-[2.5px]" />
                  <span>Following</span>
                </>
              ) : (
                <>
                  <Plus className="h-3.5 w-3.5 stroke-[2.5px]" />
                  <span>Follow Thread</span>
                </>
              )}
            </button>
          </div>

          {/* Up Next Preview section */}
          {nextArticle && onLoadNextArticle && (
            <div className="pt-8 space-y-3">
              <h4 className="text-xs font-bold font-mono tracking-widest text-outline uppercase">
                Up Next
              </h4>
              
              <button
                onClick={() => {
                  onLoadNextArticle(nextArticle);
                  if (containerRef.current) containerRef.current.scrollTop = 0;
                }}
                className="w-full text-left glass-panel p-4 rounded-2xl border border-white/5 hover:border-white/15 transition-all flex items-center gap-4 cursor-pointer group"
              >
                <div className="w-16 h-16 rounded-xl overflow-hidden shrink-0 border border-white/5">
                  <img
                    src={nextArticle.imageUrl}
                    alt={nextArticle.headline}
                    className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                  />
                </div>
                <div className="flex-1 space-y-1">
                  <span className="text-[9px] font-mono font-extrabold text-primary tracking-widest uppercase">
                    {nextArticle.category} • {nextArticle.readTime}
                  </span>
                  <p className="text-xs font-extrabold text-white line-clamp-2 leading-snug">
                    {nextArticle.headline}
                  </p>
                </div>
              </button>
            </div>
          )}

        </div>
      </div>
    </motion.div>
  );
};
