package com.example.tattle.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable data class UsernameSuggestRes(val suggested: String? = null, val alternatives: List<String> = emptyList())
@Serializable data class UsernameCheckRes(val username: String = "", val valid: Boolean = false, val available: Boolean = false, val message: String = "", val alternatives: List<String> = emptyList())
@Serializable data class UsernameClaimReq(val username: String, val name: String? = null)
@Serializable data class UsernameClaimRes(val success: Boolean = false, val message: String? = null, val username: String? = null, val alternatives: List<String> = emptyList())

@Serializable data class QuestionDto(val id: String, val text: String, val type: String = "single", val options: List<String> = emptyList())

/** A survey or an ad the admin dashboard has switched on. */
@Serializable
data class CampaignDto(
    val id: Int,
    val kind: String,
    val title: String,
    val body: String? = null,
    val imageUrl: String? = null,
    val ctaUrl: String? = null,
    val ctaLabel: String? = null,
    val durationSeconds: Int = 5,
    val questions: List<QuestionDto> = emptyList(),
    val placement: String = "checkin",          // surveys: "checkin" (the reader check-in, unlocks the pass) or "feed" (a poll between stories)
    val adFormat: String = "image",             // image | video | playable
    val mediaUrl: String? = null,               // video file or playable page (a path starting with / belongs to our own server)
    val endsAt: String? = null
)

@Serializable data class CampaignList(val campaigns: List<CampaignDto> = emptyList())
@Serializable data class PushTokenReq(val token: String, val platform: String = "android")

/** The city "Near you" is built for (from the location the reader shared). */
@Serializable data class LocalInfo(val city: String? = null, val available: Boolean = false, val region: String? = null)

@Serializable data class CampaignEventReq(val event: String)
@Serializable data class SurveyAnswersReq(val answers: Map<String, JsonElement>)
@Serializable data class SimpleOk(val success: Boolean = false, val message: String? = null, val results: Map<String, Map<String, Int>> = emptyMap())

/** What came back from sending a survey: the reason it failed (null = saved) and, for poll questions, the votes per option. */
data class SurveyOutcome(val problem: String?, val results: Map<String, Map<String, Int>> = emptyMap())

@Serializable
data class InboxItem(
    val id: Int,
    val title: String,
    val body: String,
    @SerialName("article_id") val articleId: String? = null,   // set only for a breaking-news push: lets tapping it open that story
    val sentAt: String? = null                                 // ISO-8601 UTC ("...Z"); used with setWhen() for a live "2h ago" style display
)
@Serializable data class InboxRes(val notifications: List<InboxItem> = emptyList())
