package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.MockData
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.components.ArticleCard
import com.example.tattle.ui.components.SurveyCard
import com.example.tattle.ui.theme.*
import org.koin.compose.koinInject
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    preferences: UserPreferences,
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
    
    val userSectors = preferences.interests
    var activeTab by remember { mutableStateOf(userSectors.firstOrNull() ?: "for_you") }
    var currentIndex by remember { mutableStateOf(0) }
    var showSurvey by remember { mutableStateOf(false) }
    var cardsSinceSurvey by remember { mutableStateOf(0) }
    var sessionCardsRead by remember { mutableStateOf(0) }
    val sessionCap = 10 

    val tabsList = userSectors.ifEmpty { listOf("for_you") }

    // Fetch dynamic articles
    var dynamicArticles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(activeTab) {
        if (activeTab != "saved" && activeTab != "for_you") {
            isLoading = true
            val articles = articleRepository.getArticlesBySector(activeTab)
            dynamicArticles = articles
            isLoading = false
        } else {
            dynamicArticles = emptyList()
        }
    }

    val filteredArticles = remember(activeTab, preferences.bookmarks, dynamicArticles) {
        if (dynamicArticles.isNotEmpty()) {
            dynamicArticles
        } else {
            val baseList = MockData.articles.filter { it.language == preferences.language }
            when (activeTab) {
                "saved" -> baseList.filter { preferences.bookmarks.contains(it.id) }
                "for_you" -> baseList
                else -> baseList.filter { it.category.lowercase().contains(activeTab.lowercase()) }
            }
        }
    }

    val currentArticle = filteredArticles.getOrNull(currentIndex)

    // Real-time Polling for Ollama content
    LaunchedEffect(currentArticle?.id) {
        val article = currentArticle ?: return@LaunchedEffect
        // If it's a placeholder, start polling
        if (article.hook.contains("processed by AI")) {
            while (true) {
                delay(10000) // Ping every 10 seconds to be gentle
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

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                FeedHeader(preferences = preferences, language = language, onOpenSettings = onOpenSettings)
                TabsRow(
                    tabs = tabsList,
                    activeTab = activeTab,
                    language = language,
                    onTabSelected = {
                        activeTab = it
                        currentIndex = 0
                    }
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
            if (isOffline) {
                OfflineBanner(language = language, modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp))
            }

            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            } else if (showSurvey) {
                SurveyCard(
                    survey = MockData.surveys[0],
                    onComplete = {
                        showSurvey = false
                        // Grant 3 hours ad-free (simplified)
                        onUpdatePreferences(preferences.copy(adFreeUntil = "AdFree"))
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
                        showSurvey = false
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
                    onWatchAd = {
                        showSurvey = false
                        onUpdatePreferences(preferences.copy(adFreeUntil = "AdFree"))
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
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            } else if (currentArticle == null) {
                EmptyState(activeTab, language) { activeTab = "for_you" }
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
                                onUpdatePreferences(
                                    preferences.copy(
                                        readHistory = history,
                                        totalCardsRead = preferences.totalCardsRead + 1
                                    )
                                )
                                sessionCardsRead++
                                if (sessionCardsRead >= sessionCap) {
                                    onSessionEnd()
                                } else {
                                    proceedNext(
                                        currentIndex,
                                        filteredArticles.size,
                                        { currentIndex = it },
                                        { cardsSinceSurvey = it },
                                        cardsSinceSurvey,
                                        { showSurvey = it },
                                        preferences
                                    )
                                }
                            },
                            onReaction = { emoji ->
                                val newReactions = preferences.reactions + (targetArticle.id to emoji)
                                onUpdatePreferences(preferences.copy(reactions = newReactions))
                            },
                            onImageLoaded = { url ->
                                // Update the article in the list if it's dynamic
                                if (dynamicArticles.any { it.id == targetArticle.id }) {
                                    dynamicArticles = dynamicArticles.map {
                                        if (it.id == targetArticle.id) it.copy(imageUrl = url) else it
                                    }
                                }
                            },
                            currentReaction = preferences.reactions[targetArticle.id],
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
    if (nextCardsCount >= 4 && preferences.adFreeUntil == null) {
        onShowSurvey(true)
        onSurveyCountUpdate(0)
    } else {
        onSurveyCountUpdate(nextCardsCount)
        if (currentIndex < listSize - 1) {
            onIndexUpdate(currentIndex + 1)
        } else {
            onIndexUpdate(0)
        }
    }
}

@Composable
fun FeedHeader(preferences: UserPreferences, language: String, onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = LocalStrings.get("app_name", language),
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (preferences.adFreeUntil != null) {
                Box(
                    modifier = Modifier
                        .background(Primary.copy(alpha = 0.1f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = LocalStrings.get("ad_free", language),
                        color = Primary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, null, tint = Color.Black)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.Notifications, null, tint = Color.Black)
            }
        }
    }
}

@Composable
fun TabsRow(tabs: List<String>, activeTab: String, language: String, onTabSelected: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        items(tabs) { tabKey ->
            val isActive = tabKey == activeTab
            val displayName = try {
                LocalStrings.get(tabKey, language)
            } catch (e: Exception) {
                tabKey.replaceFirstChar { it.uppercase() }
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) Primary else Color(0xFFF5F5F5))
                    .clickable { onTabSelected(tabKey) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = displayName,
                    color = if (isActive) Color.White else Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProgressLine(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(Color.Black.copy(alpha = 0.05f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Primary)
        )
    }
}

@Composable
fun OfflineBanner(language: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Surface.copy(alpha = 0.9f), RoundedCornerShape(50))
            .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.size(6.dp).background(TextMuted, CircleShape))
            Text(text = LocalStrings.get("offline_msg", language), color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun EmptyState(activeTab: String, language: String, onBrowse: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Bolt, null, tint = TextMuted, modifier = Modifier.size(40.dp))
        Text(text = LocalStrings.get("deck_empty", language), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text(
            text = if (activeTab == "saved") LocalStrings.get("no_bookmarks", language) else LocalStrings.get("new_stories_later", language),
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
        if (activeTab == "saved") {
            TextButton(onClick = onBrowse) {
                Text(text = LocalStrings.get("browse_feed", language), color = Primary, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}
