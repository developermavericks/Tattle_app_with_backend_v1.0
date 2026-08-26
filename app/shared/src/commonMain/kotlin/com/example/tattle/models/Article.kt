package com.example.tattle.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SectorResponse(
    @SerialName("sectors") val sectors: List<String>
)

@Serializable
data class CuratedArticle(
    @SerialName("id") val id: Int,
    @SerialName("article_id") val articleId: String,
    @SerialName("headline") val headline: String,
    @SerialName("url") val url: String?,
    @SerialName("source") val source: String?,
    @SerialName("published_at") val publishedAt: String?,
    @SerialName("sector") val sector: String,
    @SerialName("rank") val rank: Int,
    @SerialName("final_score") val finalScore: Double,
    @SerialName("age_bracket") val ageBracket: String,
    @SerialName("curated_date") val curatedDate: String,
    @SerialName("is_duplicate") val isDuplicate: Boolean,
    @SerialName("confidence_score") val confidenceScore: Double?,
    
    // --- Ollama-Enriched Fields ---
    @SerialName("ollama_processed") val ollamaProcessed: Boolean,
    @SerialName("summary") val summary: String?,
    @SerialName("key_takeaways") val keyTakeaways: List<String>?,
    @SerialName("why_it_matters") val whyItMatters: String?,
    @SerialName("cleaned_content") val cleanedContent: String?
)

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
    val language: String = "English"
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
