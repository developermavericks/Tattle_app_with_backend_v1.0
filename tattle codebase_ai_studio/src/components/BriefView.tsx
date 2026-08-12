/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useEffect, useRef, useState } from "react";
import { Article } from "../types";
import { Share2, Bookmark, X, Terminal, ArrowUpRight, Award, Zap, Shield, Cpu, Sparkles } from "lucide-react";
import { motion } from "motion/react";

interface BriefViewProps {
  article: Article;
  isBookmarked: boolean;
  onClose: () => void;
  onToggleBookmark: () => void;
  onShare: () => void;
  onLaunchFullText: () => void;
}

export const BriefView: React.FC<BriefViewProps> = ({
  article,
  isBookmarked,
  onClose,
  onToggleBookmark,
  onShare,
  onLaunchFullText
}) => {
  const panelRef = useRef<HTMLDivElement>(null);
  const [startY, setStartY] = useState<number>(0);
  const [currentY, setCurrentY] = useState<number>(0);
  const [isDragging, setIsDragging] = useState<boolean>(false);

  // Swipe-to-dismiss touch/mouse tracking
  const handleTouchStart = (e: React.TouchEvent) => {
    setStartY(e.touches[0].clientY);
    setIsDragging(true);
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isDragging) return;
    const diff = e.touches[0].clientY - startY;
    if (diff > 0) {
      setCurrentY(diff);
    }
  };

  const handleTouchEnd = () => {
    setIsDragging(false);
    if (currentY > 150) {
      onClose();
    } else {
      setCurrentY(0);
    }
  };

  // Drag handling for desktop mouse
  const handleMouseDown = (e: React.MouseEvent) => {
    setStartY(e.clientY);
    setIsDragging(true);
  };

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (!isDragging) return;
      const diff = e.clientY - startY;
      if (diff > 0) {
        setCurrentY(diff);
      }
    };

    const handleMouseUp = () => {
      if (isDragging) {
        setIsDragging(false);
        if (currentY > 150) {
          onClose();
        } else {
          setCurrentY(0);
        }
      }
    };

    window.addEventListener("mousemove", handleMouseMove);
    window.addEventListener("mouseup", handleMouseUp);

    return () => {
      window.removeEventListener("mousemove", handleMouseMove);
      window.removeEventListener("mouseup", handleMouseUp);
    };
  }, [isDragging, startY, currentY, onClose]);

  // Map bullet points to rich display icons based on keywords
  const getBulletIcon = (index: number) => {
    if (index === 0) return <Cpu className="text-primary h-5 w-5 shrink-0" />;
    if (index === 1) return <Zap className="text-secondary h-5 w-5 shrink-0" />;
    return <Shield className="text-on-surface h-5 w-5 shrink-0" />;
  };

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 bg-background/80 backdrop-blur-md z-50 flex flex-col justify-end"
    >
      {/* Background close click */}
      <div className="absolute inset-0 z-0" onClick={onClose} />

      {/* Draggable panel */}
      <motion.div
        ref={panelRef}
        initial={{ y: "100%" }}
        animate={{ y: currentY }}
        exit={{ y: "100%" }}
        transition={isDragging ? { type: "tween" } : { type: "spring", damping: 25, stiffness: 220 }}
        className="relative z-10 w-full max-w-lg mx-auto bg-background rounded-t-3xl border-t border-white/10 flex flex-col max-h-[92vh] overflow-hidden shadow-2xl"
      >
        {/* Gesture Drag Bar */}
        <div
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          onMouseDown={handleMouseDown}
          className="w-full flex flex-col items-center pt-3 pb-2 cursor-grab active:cursor-grabbing shrink-0 hover:bg-white/5 transition-colors"
        >
          <div className="drag-handle" />
        </div>

        {/* Header toolbar */}
        <header className="w-full px-6 py-2 flex items-center justify-between shrink-0 border-b border-white/5">
          <div className="flex items-center gap-2">
            {/* Tiny stylized logo */}
            <div className="w-6 h-6 rounded-lg bg-gradient-to-tr from-primary to-secondary p-0.5 flex items-center justify-center">
              <span className="font-extrabold text-[12px] text-black">T</span>
            </div>
            <span className="text-sm font-extrabold tracking-widest text-primary font-mono">
              TATTLE BRIEF
            </span>
          </div>
          
          <button
            onClick={onClose}
            className="p-1.5 rounded-full hover:bg-surface-variant text-outline hover:text-white transition-colors cursor-pointer"
          >
            <X className="h-5 w-5" />
          </button>
        </header>

        {/* Scrollable Brief Body Content */}
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6 no-scrollbar">
          
          {/* Tag & Time Headers */}
          <div className="flex items-center gap-2.5">
            {article.isBreaking && (
              <span className="bg-secondary-container text-on-secondary-container px-2.5 py-0.5 rounded text-[10px] font-mono font-extrabold tracking-widest uppercase">
                BREAKING
              </span>
            )}
            <span className="text-primary text-[10px] font-mono font-extrabold tracking-widest uppercase bg-primary/10 px-2 py-0.5 rounded">
              {article.category.toUpperCase()}
            </span>
            <span className="text-text-muted text-[10px] font-mono font-bold tracking-widest uppercase">
              {article.readTime.toUpperCase()}
            </span>
          </div>

          {/* Headline */}
          <h2 className="text-2xl font-extrabold text-white leading-tight font-sans">
            {article.headline}
          </h2>

          {/* Core Summary Line */}
          <div className="border-l-4 border-primary pl-4 py-1">
            <p className="text-base font-medium leading-relaxed text-white">
              {article.brief}
            </p>
          </div>

          {/* Bento Grid Bullet Points */}
          <section className="space-y-3">
            <h3 className="text-xs font-bold font-mono tracking-widest text-outline uppercase">
              Core Takeaways
            </h3>
            <div className="grid grid-cols-1 gap-3">
              {article.bullets.map((bullet, idx) => {
                // Parse key elements in bullet points for bolding (e.g. split by first colon if available)
                const parts = bullet.split(": ");
                const hasTitle = parts.length > 1;
                
                return (
                  <div key={idx} className="glass-panel p-4 rounded-xl flex items-start gap-3.5 border border-white/5">
                    {getBulletIcon(idx)}
                    <div className="space-y-0.5">
                      {hasTitle ? (
                        <p className="text-xs font-bold font-mono text-primary uppercase tracking-wide">
                          {parts[0]}
                        </p>
                      ) : null}
                      <p className="text-xs text-text-muted leading-relaxed font-medium">
                        {hasTitle ? parts.slice(1).join(": ") : bullet}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </section>

          {/* Neon Bordered 'Why It Matters' */}
          <div className="relative neon-border bg-primary/5 p-5 rounded-2xl overflow-hidden group">
            {/* Decorative background logo */}
            <div className="absolute -right-6 -top-6 opacity-[0.03] text-primary pointer-events-none group-hover:scale-105 transition-transform duration-500">
              <Sparkles className="h-28 w-28" />
            </div>

            <div className="flex items-center gap-2 mb-3 z-10 relative">
              <Zap className="text-primary h-5 w-5 fill-primary" />
              <h4 className="text-sm font-extrabold tracking-widest text-primary font-mono uppercase">
                Why It Matters
              </h4>
            </div>

            <p className="text-xs text-on-surface leading-relaxed relative z-10 font-medium">
              {article.whyItMatters}
            </p>
          </div>

          {/* Full Text Launch Trigger Link */}
          <div className="text-center pt-2">
            <button
              onClick={onLaunchFullText}
              className="inline-flex items-center gap-1.5 text-xs font-bold font-mono text-primary hover:text-primary-light hover:underline transition-all cursor-pointer"
            >
              LAUNCH FULL-DEPTH REPORT
              <ArrowUpRight className="h-4 w-4" />
            </button>
          </div>
        </div>

        {/* Bottom Sticky Action Bar */}
        <div className="p-5 border-t border-white/5 bg-background/90 backdrop-blur-md flex gap-4 shrink-0">
          <button
            onClick={onShare}
            className="flex-1 h-14 bg-primary hover:bg-primary-light text-black font-semibold text-xs tracking-widest uppercase rounded-full flex items-center justify-center gap-2 transition-all duration-200 cursor-pointer shadow-lg shadow-primary/10 active:scale-98"
          >
            <Share2 className="h-4 w-4 stroke-[2.5px]" />
            <span>Share Insight</span>
          </button>
          
          <button
            onClick={onToggleBookmark}
            className={`w-14 h-14 rounded-full flex items-center justify-center transition-all duration-200 cursor-pointer border ${
              isBookmarked
                ? "border-secondary text-secondary bg-secondary/15"
                : "border-white/10 text-text-muted hover:border-white/20 hover:text-white"
            }`}
          >
            <Bookmark className={`h-5 w-5 ${isBookmarked ? "fill-secondary" : ""}`} />
          </button>
        </div>
      </motion.div>
    </motion.div>
  );
};
