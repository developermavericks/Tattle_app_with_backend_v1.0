/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Article {
  id: string;
  category: string;
  readTime: string;
  headline: string;
  hook: string;
  brief: string;
  bullets: string[];
  whyItMatters: string;
  fullText: string;
  publisher: string;
  publishedAt: string;
  imageUrl: string;
  likes: number;
  commentsCount: number;
  isBreaking: boolean;
  views: number;
}

export interface UserPreferences {
  isOnboarded: boolean;
  ageGroup: string;
  interests: string[];
  language: string;
  notifications: {
    dailyBriefings: boolean;
    breakingAlerts: boolean;
    streaks: boolean;
  };
  streak: number;
  totalCardsRead: number;
  readHistory: string[]; // List of read article IDs
  bookmarks: string[]; // List of bookmarked article IDs
  reactions: { [articleId: string]: string }; // Map of articleId to emoji reaction
  adFreeUntil: string | null; // ISO Timestamp or null
  sensitivity: "Strict" | "Standard" | "Minimal";
  lastReadDate: string | null; // YYYY-MM-DD
}

export interface SurveyQuestion {
  id: string;
  question: string;
  options: string[];
}

export interface Survey {
  id: string;
  title: string;
  questions: SurveyQuestion[];
}
