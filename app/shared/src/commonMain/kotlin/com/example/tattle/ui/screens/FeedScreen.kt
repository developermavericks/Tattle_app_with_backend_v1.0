package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import io.kamel.image.KamelImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import tattle.app.shared.generated.resources.*
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.MockData
import com.example.tattle.data.sortProcessedFirst
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.components.AdWatchDialog
import com.example.tattle.ui.components.ArticleCard
import com.example.tattle.ui.components.CheckInChoiceDialog
import com.example.tattle.ui.components.SurveyCard
import com.example.tattle.ui.theme.*
import com.example.tattle.utils.currentTimeMillis
import org.koin.compose.koinInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

@Composable
fun FeedScreen(
    preferences: UserPreferences,
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onUpdatePreferences: (UserPreferences) -> Unit,
    onOpenArticle: (Article) -> Unit,
    onLaunchBrief: (Article) -> Unit,
    onShareArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit,
    onSessionEnd: () -> Unit,
    isOffline: Boolean = false
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    val imageRepository = koinInject<ImageRepository>()
    
    val userSectors = preferences.interests
    var currentIndex by remember(activeTab) { mutableStateOf(0) }
    var showSurvey by remember { mutableStateOf(false) }
    var showCheckInChoice by remember { mutableStateOf(false) }
    var showSurveyCard by remember { mutableStateOf(false) }
    var showAdPlayer by remember { mutableStateOf(false) }
    var cardsSinceSurvey by remember { mutableStateOf(0) }

    LaunchedEffect(showSurvey) {
        if (showSurvey) {
            showCheckInChoice = true
        }
    }

    // Instant 0ms seed initialization for immediate screen render
    val initialSeed = remember(activeTab) {
        when (activeTab) {
            "saved" -> articleRepository.getArticlesByIds(preferences.bookmarks)
            "for_you" -> MockData.articles.sortProcessedFirst()
            else -> MockData.getArticlesBySector(activeTab).sortProcessedFirst()
        }
    }
    var dynamicArticles by remember(activeTab) { mutableStateOf(initialSeed) }
    var seenArticleIds by remember(activeTab) { mutableStateOf(initialSeed.map { it.id }.toSet()) }

    // Background sync: Updates feed with fresh remote articles from Ktor server
    LaunchedEffect(activeTab, preferences.bookmarks) {
        val remoteBatch = when (activeTab) {
            "saved" -> articleRepository.getArticlesByIdsFromBackend(preferences.bookmarks)
            "for_you" -> articleRepository.getForYouArticles(userSectors)
            else -> articleRepository.getArticlesBySector(activeTab)
        }

        if (remoteBatch.isNotEmpty()) {
            val sortedBatch = remoteBatch.distinctBy { it.id }.sortProcessedFirst()
            dynamicArticles = sortedBatch
            seenArticleIds = sortedBatch.map { it.id }.toSet()
        }
    }

    // Dynamic Endless 15-Card Buffer Polling: Triggers when remaining cards <= 10 to keep 15+ cards ahead
    LaunchedEffect(currentIndex, dynamicArticles.size, activeTab) {
        if (activeTab != "saved" && dynamicArticles.isNotEmpty() && (dynamicArticles.size - currentIndex <= 10)) {
            val fetchedBatch = when (activeTab) {
                "for_you" -> articleRepository.getForYouArticles(emptyList())
                else -> articleRepository.getArticlesBySector("all")
            }

            val unseenNewCards = fetchedBatch.filter { !seenArticleIds.contains(it.id) }.sortProcessedFirst()
            if (unseenNewCards.isNotEmpty()) {
                dynamicArticles = dynamicArticles + unseenNewCards
                seenArticleIds = seenArticleIds + unseenNewCards.map { it.id }
            }
        }
    }

    val filteredArticles = dynamicArticles
    val currentArticle = filteredArticles.getOrNull(currentIndex)

    // Pre-fetch & decode ImageBitmaps for next 15 articles in background for 0ms swiping latency
    LaunchedEffect(dynamicArticles, currentIndex) {
        if (dynamicArticles.isNotEmpty()) {
            val upcomingBatch = dynamicArticles.drop(currentIndex).take(15)
            upcomingBatch.forEach { article ->
                launch {
                    imageRepository.getOrFetchBitmapForArticle(article)
                }
            }
        }
    }

    // Real-time Polling for Ollama content
    LaunchedEffect(currentArticle?.id) {
        val article = currentArticle ?: return@LaunchedEffect
        if (article.hook.contains("processed by AI", ignoreCase = true)) {
            while (true) {
                delay(10000)
                val updatedArticle = articleRepository.pollArticleUpdate(activeTab, article.id)
                if (updatedArticle != null) {
                    dynamicArticles = dynamicArticles.map {
                        if (it.id == updatedArticle.id) updatedArticle else it
                    }
                    break
                }
            }
        }
    }

    val nowMillis = currentTimeMillis()
    val isLockedOut = preferences.lockoutUntil != null && preferences.lockoutUntil > nowMillis
    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White

    Scaffold(
        containerColor = bgColor,
        topBar = {
            Column(modifier = Modifier.background(bgColor)) {
                FeedHeader(
                    preferences = preferences,
                    language = language,
                    onOpenSettings = { onOpenSettings() },
                    onOpenRecap = { onSessionEnd() }
                )
                ProgressLine(
                    progress = if (filteredArticles.isNotEmpty()) (currentIndex + 1).toFloat() / filteredArticles.size else 0f
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLockedOut) {
                LockoutScreen(
                    lockoutUntil = preferences.lockoutUntil!!,
                    onUnlockViaSurvey = {
                        showSurvey = true
                        showCheckInChoice = true
                    },
                    modifier = Modifier.padding(24.dp)
                )
            } else if (showCheckInChoice) {
                CheckInChoiceDialog(
                    onSelectWatchAd = {
                        showCheckInChoice = false
                        showAdPlayer = true
                    },
                    onSelectTakeSurvey = {
                        showCheckInChoice = false
                        showSurveyCard = true
                    },
                    isDark = isDark
                )
            } else if (showAdPlayer) {
                AdWatchDialog(
                    onAdCompleted = {
                        showAdPlayer = false
                        showSurvey = false
                        val passExpiry = (currentTimeMillis() + 3600 * 1000L).toString()
                        onUpdatePreferences(preferences.copy(lockoutUntil = null, adFreeUntil = passExpiry))
                        proceedNext(
                            currentIndex,
                            filteredArticles.size,
                            { currentIndex = it },
                            { cardsSinceSurvey = it },
                            cardsSinceSurvey,
                            { showSurvey = it },
                            preferences
                        )
                    },
                    onDismiss = {
                        showAdPlayer = false
                        showCheckInChoice = true
                    }
                )
            } else if (showSurveyCard) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    SurveyCard(
                        survey = MockData.surveys.first(),
                        onComplete = {
                            showSurveyCard = false
                            showSurvey = false
                            val passExpiry = (currentTimeMillis() + 3600 * 1000L).toString()
                            onUpdatePreferences(preferences.copy(lockoutUntil = null, adFreeUntil = passExpiry))
                            proceedNext(
                                currentIndex,
                                filteredArticles.size,
                                { currentIndex = it },
                                { cardsSinceSurvey = it },
                                cardsSinceSurvey,
                                { showSurvey = it },
                                preferences
                            )
                        },
                        onSkip = {
                            showSurveyCard = false
                            showCheckInChoice = true
                        },
                        isDark = isDark
                    )
                }
            } else if (currentArticle == null) {
                EmptyState(activeTab, language) { onTabSelected("for_you") }
            } else {
                AnimatedContent(
                    targetState = currentArticle,
                    transitionSpec = {
                        if (targetState.id != (initialState?.id ?: "")) {
                            (fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400)))
                                .togetherWith(fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.92f, animationSpec = tween(300)))
                        } else {
                            fadeIn(animationSpec = tween(400)).togetherWith(fadeOut(animationSpec = tween(400)))
                        }
                    },
                    label = "ArticleTransition"
                ) { targetArticle ->
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        ArticleCard(
                            article = targetArticle,
                            isBookmarked = preferences.bookmarks.contains(targetArticle.id),
                            onToggleBookmark = {
                                val newBookmarks = if (preferences.bookmarks.contains(targetArticle.id)) {
                                    preferences.bookmarks - targetArticle.id
                                } else {
                                    preferences.bookmarks + targetArticle.id
                                }
                                onUpdatePreferences(preferences.copy(bookmarks = newBookmarks))
                            },
                            onShare = { onShareArticle(targetArticle) },
                            onOpenBrief = { onLaunchBrief(targetArticle) },
                            onOpenFull = { onOpenArticle(targetArticle) },
                            onSwipeLeft = {
                                proceedNext(
                                    currentIndex,
                                    filteredArticles.size,
                                    { currentIndex = it },
                                    { cardsSinceSurvey = it },
                                    cardsSinceSurvey,
                                    { showSurvey = it },
                                    preferences
                                )
                            },
                            onSwipeRight = {
                                val history = if (!preferences.readHistory.contains(targetArticle.id)) {
                                    preferences.readHistory + targetArticle.id
                                } else {
                                    preferences.readHistory
                                }
                                onUpdatePreferences(preferences.copy(readHistory = history))
                                proceedNext(
                                    currentIndex,
                                    filteredArticles.size,
                                    { currentIndex = it },
                                    { cardsSinceSurvey = it },
                                    cardsSinceSurvey,
                                    { showSurvey = it },
                                    preferences
                                )
                            },
                            onSwipeUp = {
                                proceedNext(
                                    currentIndex,
                                    filteredArticles.size,
                                    { currentIndex = it },
                                    { cardsSinceSurvey = it },
                                    cardsSinceSurvey,
                                    { showSurvey = it },
                                    preferences
                                )
                            },
                            onReaction = { reaction ->
                                val newReactions = preferences.reactions.toMutableMap()
                                newReactions[targetArticle.id] = reaction
                                onUpdatePreferences(preferences.copy(reactions = newReactions))
                            },
                            onImageLoaded = { },
                            currentReaction = preferences.reactions[targetArticle.id],
                            isDark = isDark,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

private fun proceedNext(
    currentIndex: Int,
    listSize: Int,
    onIndexUpdate: (Int) -> Unit,
    onSurveyCountUpdate: (Int) -> Unit,
    cardsSinceSurvey: Int,
    onShowSurvey: (Boolean) -> Unit,
    preferences: UserPreferences
) {
    val nextCardsCount = cardsSinceSurvey + 1
    val now = currentTimeMillis()
    val adFreeUntilTime = preferences.adFreeUntil?.toLongOrNull() ?: 0L
    val is1HourPassActive = preferences.adFreeUntil == "AdFree" || adFreeUntilTime > now

    if (nextCardsCount >= 5 && !is1HourPassActive) {
        onShowSurvey(true)
        onSurveyCountUpdate(0)
    } else {
        onSurveyCountUpdate(nextCardsCount)
        if (currentIndex < listSize - 1) {
            onIndexUpdate(currentIndex + 1)
        }
    }
}

@Composable
fun LockoutScreen(
    lockoutUntil: Long,
    onUnlockViaSurvey: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingMillis = (lockoutUntil - currentTimeMillis()).coerceAtLeast(0)
    val remainingHours = remainingMillis / (3600 * 1000)
    val remainingMins = (remainingMillis % (3600 * 1000)) / (60 * 1000)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.HourglassTop, null, tint = Primary, modifier = Modifier.size(36.dp))
            }

            Text(
                text = "3-Hour Cool Off",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "You skipped the survey. Come back in $remainingHours hrs $remainingMins mins to continue reading news, or complete the quick survey now to unlock immediately!",
                fontSize = 15.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onUnlockViaSurvey,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Assignment, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Take Survey & Unlock Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FeedHeader(
    preferences: UserPreferences,
    language: String,
    onOpenSettings: () -> Unit,
    onOpenRecap: () -> Unit
) {
    val isDark = preferences.isDarkMode
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(
                painter = painterResource(Res.drawable.logo),
                contentDescription = "Tattle Logo",
                modifier = Modifier.height(36.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(if (isDark) DarkTextPrimary else Color.Black)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Daily Recap Button at Top
            Surface(
                onClick = onOpenRecap,
                shape = RoundedCornerShape(20.dp),
                color = Primary.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = "Daily Recap", tint = Primary, modifier = Modifier.size(16.dp))
                    Text("Recap", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            val now = currentTimeMillis()
            val adFreeUntilTime = preferences.adFreeUntil?.toLongOrNull() ?: 0L
            val is1HourPassActive = preferences.adFreeUntil == "AdFree" || adFreeUntilTime > now

            if (is1HourPassActive) {
                Box(
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .border(1.dp, Primary.copy(alpha = 0.3f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "⚡ 1-Hour Pass",
                        color = Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, null, tint = if (isDark) DarkTextPrimary else Color.Black)
            }
        }
    }
}

@Composable
fun ProgressLine(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(2.dp),
        color = Primary,
        trackColor = Color.Transparent
    )
}

@Composable
fun EmptyState(activeTab: String, language: String, onReset: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Inbox, null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Text(
            text = "All caught up on $activeTab!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
            Text("Back to For You Feed")
        }
    }
}
