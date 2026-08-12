package com.example.tattle.models

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val id: String,
    val category: String,
    val readTime: String,
    val headline: String,
    val hook: String,
    val brief: String,
    val bullets: List<String>,
    val whyItMatters: String,
    val fullText: String,
    val publisher: String,
    val publishedAt: String,
    val imageUrl: String,
    val likes: Int,
    val commentsCount: Int,
    val isBreaking: Boolean,
    val views: Int,
)

@Serializable
data class SurveyQuestion(
    val id: String,
    val question: String,
    val options: List<String>
)

@Serializable
data class Survey(
    val id: String,
    val title: String,
    val questions: List<SurveyQuestion>
)

@Serializable
data class NotificationsPrefs(
    val dailyBriefings: Boolean,
    val breakingAlerts: Boolean,
    val streaks: Boolean
)

@Serializable
data class UserPreferences(
    val isOnboarded: Boolean,
    val ageGroup: String,
    val interests: List<String>,
    val language: String,
    val notifications: NotificationsPrefs,
    val streak: Int,
    val totalCardsRead: Int,
    val readHistory: List<String>,
    val bookmarks: List<String>,
    val reactions: Map<String, String>,
    val adFreeUntil: String?,
    val sensitivity: String,
    val lastReadDate: String?
)
