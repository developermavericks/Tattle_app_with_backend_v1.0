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
import androidx.compose.ui.graphics.graphicsLayer
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
import com.example.tattle.ui.components.CardDeck
import com.example.tattle.ui.components.OfflineBanner
import com.example.tattle.ui.components.SkeletonCard
import com.example.tattle.ui.components.SwipeDir
import com.example.tattle.ui.components.CheckInChoiceDialog
import com.example.tattle.ui.components.ServerAdDialog
import com.example.tattle.ui.components.ServerSurveyCard
import com.example.tattle.ui.components.SurveyCard
import com.example.tattle.ui.theme.*
import com.example.tattle.utils.currentTimeMillis
import org.koin.compose.koinInject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.launch
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
    val userSync = koinInject<com.example.tattle.data.UserSync>()
    
    val userSectors = preferences.interests
    var currentIndex by remember(activeTab) { mutableStateOf(0) }
    var showSurvey by remember { mutableStateOf(false) }
    var showCheckInChoice by remember { mutableStateOf(false) }
    var showSurveyCard by remember { mutableStateOf(false) }
    var showAdPlayer by remember { mutableStateOf(false) }
    var cardsSinceSurvey by remember { mutableStateOf(0) }

    // Surveys and ads come from the admin dashboard. Nothing is shown when nothing is switched on.
    var campaigns by remember { mutableStateOf<List<com.example.tattle.data.CampaignDto>>(emptyList()) }
    val adCampaign = campaigns.firstOrNull { it.kind == "ad" }
    val surveyCampaign = campaigns.firstOrNull { it.kind == "survey" && it.placement != "feed" }
    // a poll the admin placed inside the feed: shown now and then between stories, never tied to the 1-hour pass
    var skippedPolls by remember { mutableStateOf<Set<Int>>(emptySet()) }
    val feedPoll = campaigns.firstOrNull { it.kind == "survey" && it.placement == "feed" && it.id !in skippedPolls }
    var cardsSincePoll by remember { mutableStateOf(0) }
    var showFeedPoll by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            campaigns = userSync.fetchCampaigns()
            delay(300_000)
        }
    }

    LaunchedEffect(showSurvey) {
        if (showSurvey) {
            when {
                adCampaign != null && surveyCampaign != null -> showCheckInChoice = true       // the person picks
                surveyCampaign != null -> showSurveyCard = true
                adCampaign != null -> showAdPlayer = true
                else -> showSurvey = false
            }
        }
    }


    // Instant 0ms seed initialization for immediate screen render
    val initialSeed = remember(activeTab) {
        when (activeTab) {
            "saved" -> articleRepository.getArticlesByIds(preferences.bookmarks)
            // the stories this phone got last time (unseen ones only): they show instantly, then the fresh batch replaces them quietly
            else -> articleRepository.cachedFeed(activeTab).filter { it.id !in preferences.seenIds }
                .ifEmpty { articleRepository.offlineStories(activeTab, preferences.seenIds).take(20) }       // a first screen even with no cache and no connection
                .let { if (activeTab == "for_you") it.shuffled() else it }   // the cached order itself is fixed, so this is what actually randomizes the very first thing shown on open
        }
    }
    var loadingFirst by remember(activeTab) { mutableStateOf(activeTab != "saved" && initialSeed.isEmpty()) }
    var dynamicArticles by remember(activeTab) { mutableStateOf(initialSeed) }
    var seenArticleIds by remember(activeTab) { mutableStateOf(initialSeed.map { it.id }.toSet()) }

    // Background sync: Updates feed with fresh remote articles from Ktor server
    LaunchedEffect(activeTab, preferences.bookmarks) {
        val remoteBatch = when (activeTab) {
            "saved" -> articleRepository.getArticlesByIdsFromBackend(preferences.bookmarks)
            "for_you" -> articleRepository.getForYouArticles(userSectors, preferences.seenIds)
            "near_you" -> articleRepository.getLocalArticles(preferences.seenIds, preferences.genZMode)
            else -> articleRepository.getArticlesBySector(activeTab, preferences.seenIds, preferences.genZMode)
        }
        if (activeTab != "saved") launch { imageRepository.warmDisk(remoteBatch.drop(15).take(45)) }      // the rest of the batch is kept on the phone too, pictures included

        // a story already read (even offline, before the server knew) never comes back: the phone's own list of read stories filters every batch
        val readIds = preferences.seenIds.toSet()
        val sortedBatch = remoteBatch.distinctBy { it.id }.let { b -> if (activeTab == "saved") b else b.filter { it.id !in readIds } }          // the server's order: newspaper stories first, then Nexus
        if (sortedBatch.isNotEmpty()) {
            if (currentIndex == 0) {
                if (dynamicArticles.isEmpty()) {
                    // the first pictures are ready before their cards appear (but never wait more than 1.5 s for a slow connection)
                    withTimeoutOrNull(1500) {
                        coroutineScope { sortedBatch.take(3).map { async { imageRepository.getOrFetchBitmapForArticle(it) } }.awaitAll() }
                    }
                }
                // For You stays fresh: a different order every time the app is opened (sector tabs keep the server's relevance order)
                dynamicArticles = if (activeTab == "for_you") sortedBatch.shuffled() else sortedBatch
                seenArticleIds = sortedBatch.map { it.id }.toSet()
            } else {
                // the person has already started reading: never swap the deck under them, only add what is new
                val fresh = sortedBatch.filter { n -> dynamicArticles.none { it.id == n.id } }.let { if (activeTab == "for_you") it.shuffled() else it }
                if (fresh.isNotEmpty()) {
                    dynamicArticles = dynamicArticles + fresh
                    seenArticleIds = seenArticleIds + fresh.map { it.id }
                }
            }
        }
        loadingFirst = false
    }

    // A connection that comes and goes (a train, a tunnel): one loop keeps the deck current. Offline it asks every 12 seconds, online every 90.
    // Stories that arrive after a gap go in right after the card being read; the ones already read are never offered again.
    val offlineNow by articleRepository.isOffline.collectAsState()
    var addedWhileAway by remember { mutableIntStateOf(0) }
    val latestPrefs by rememberUpdatedState(preferences)
    var sawOffline by remember { mutableStateOf(articleRepository.isOffline.value) }          // set the moment the connection is found gone, even by another request
    LaunchedEffect(offlineNow) { if (offlineNow) sawOffline = true }
    LaunchedEffect(activeTab) {
        if (activeTab == "saved") return@LaunchedEffect
        while (true) {
            var waited = 0
            while (waited < (if (articleRepository.isOffline.value) 12 else 90)) { delay(3_000); waited += 3 }       // the wait shortens as soon as the connection is found gone
            val wasOffline = sawOffline
            val p = latestPrefs
            val batch = when (activeTab) {
                "for_you" -> articleRepository.getForYouArticles(userSectors, p.seenIds)
                "near_you" -> articleRepository.getLocalArticles(p.seenIds, p.genZMode)
                else -> articleRepository.getArticlesBySector(activeTab, p.seenIds, p.genZMode)
            }
            val nowOffline = articleRepository.isOffline.value
            if (!nowOffline) {
                val read = p.seenIds.toSet()
                val fresh = batch.distinctBy { it.id }.filter { n -> n.id !in read && dynamicArticles.none { it.id == n.id } }
                if (fresh.isNotEmpty()) {
                    if (wasOffline) {
                        val at = (currentIndex + 1).coerceAtMost(dynamicArticles.size)
                        dynamicArticles = dynamicArticles.take(at) + fresh + dynamicArticles.drop(at)
                        addedWhileAway = fresh.size
                    } else if (dynamicArticles.size - currentIndex < 30) {
                        dynamicArticles = dynamicArticles + fresh
                    }
                    seenArticleIds = seenArticleIds + fresh.map { it.id }
                    launch { imageRepository.warmDisk(fresh.take(45)) }
                }
                if (wasOffline) userSync.syncBookmarks()                       // bookmarks made while offline reach the server now
                sawOffline = false
            }
        }
    }

    // While online, keep about 80 unread stories (and their pictures) on the phone, so a long stretch without a connection does not run out.
    LaunchedEffect(activeTab, offlineNow) {
        if (offlineNow || activeTab == "saved") return@LaunchedEffect
        delay(6_000)
        while (true) {
            val p = latestPrefs
            articleRepository.topUpLibrary(p.interests, p.seenIds)
            imageRepository.warmDisk(articleRepository.offlineStories(null, p.seenIds, 100))
            delay(240_000)
        }
    }
    LaunchedEffect(addedWhileAway) {
        if (addedWhileAway > 0) { delay(4000); addedWhileAway = 0 }
    }

    // For You keeps reshuffling itself every 15 minutes the reader stays on this tab, so it never feels like the same fixed order all day.
    // Only the upcoming, not-yet-seen cards move - whatever is already open stays put under the reader.
    LaunchedEffect(activeTab) {
        if (activeTab != "for_you") return@LaunchedEffect
        while (true) {
            delay(15 * 60_000L)
            if (dynamicArticles.size > currentIndex + 2) {
                dynamicArticles = dynamicArticles.take(currentIndex + 1) + dynamicArticles.drop(currentIndex + 1).shuffled()
            }
        }
    }

    // Dynamic Endless 15-Card Buffer Polling: Triggers when remaining cards <= 10 to keep 15+ cards ahead
    LaunchedEffect(currentIndex, dynamicArticles.size, activeTab) {
        if (activeTab != "saved" && dynamicArticles.isNotEmpty() && (dynamicArticles.size - currentIndex <= 10)) {
            val fetchedBatch = when (activeTab) {
                "for_you" -> articleRepository.getForYouArticles(emptyList(), preferences.seenIds + seenArticleIds)
                "near_you" -> articleRepository.getLocalArticles(preferences.seenIds + seenArticleIds, preferences.genZMode)
                else -> articleRepository.getArticlesBySector(activeTab, preferences.seenIds + seenArticleIds, preferences.genZMode)
            }

            val unseenNewCards = fetchedBatch.distinctBy { it.id }
                .filter { it.id !in seenArticleIds && dynamicArticles.none { d -> d.id == it.id } }
                .let { if (activeTab == "for_you") it.shuffled() else it }
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
                    adTitle = adCampaign?.title ?: "Watch a short ad",
                    surveyTitle = surveyCampaign?.title ?: "Take a quick survey",
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
            } else if (showAdPlayer && adCampaign != null) {
                ServerAdDialog(
                    campaign = adCampaign,
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
                            preferences,
                            campaigns.isNotEmpty()
                        )
                    },
                    onDismiss = {
                        showAdPlayer = false
                        showCheckInChoice = true
                    }
                )
            } else if (showSurveyCard && surveyCampaign != null) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    ServerSurveyCard(
                        campaign = surveyCampaign,
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
                                preferences,
                                campaigns.isNotEmpty()
                            )
                        },
                        onSkip = {
                            showSurveyCard = false
                            if (adCampaign != null) {
                                showCheckInChoice = true
                            } else {
                                showSurvey = false          // nothing else to offer: carry on reading (no pass)
                                proceedNext(
                                    currentIndex,
                                    filteredArticles.size,
                                    { currentIndex = it },
                                    { cardsSinceSurvey = it },
                                    cardsSinceSurvey,
                                    { showSurvey = it },
                                    preferences,
                                    false
                                )
                            }
                        },
                        isDark = isDark
                    )
                }
            } else if (showFeedPoll && feedPoll != null) {
                Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
                    ServerSurveyCard(
                        campaign = feedPoll,
                        grantsPass = false,
                        onComplete = { skippedPolls = skippedPolls + feedPoll.id; showFeedPoll = false; cardsSincePoll = 0 },
                        onSkip = { skippedPolls = skippedPolls + feedPoll.id; showFeedPoll = false; cardsSincePoll = 0 },
                        isDark = isDark
                    )
                }
            } else if (currentArticle == null && loadingFirst) {
                SkeletonCard(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp))
            } else if (currentArticle == null) {
                EmptyState(activeTab, language, offline = offlineNow) { onTabSelected("for_you") }
            } else {
                CardDeck(
                    articles = filteredArticles,
                    currentIndex = currentIndex,
                    bookmarks = preferences.bookmarks,
                    isDark = isDark,
                    onToggleBookmark = { article ->
                        val newBookmarks = if (preferences.bookmarks.contains(article.id)) preferences.bookmarks - article.id else preferences.bookmarks + article.id
                        onUpdatePreferences(preferences.copy(bookmarks = newBookmarks))
                    },
                    onShare = { onShareArticle(it) },
                    onOpenBrief = { onLaunchBrief(it) },
                    onOpenFull = { onOpenArticle(it) },
                    onSwipe = { article, dir ->
                        when (dir) {
                            SwipeDir.LEFT, SwipeDir.UP -> {
                                onUpdatePreferences(markSeen(preferences, article.id))
                                cardsSincePoll++
                                if (cardsSincePoll >= 8 && feedPoll != null) showFeedPoll = true
                                proceedNext(
                                    currentIndex, filteredArticles.size, { currentIndex = it }, { cardsSinceSurvey = it },
                                    cardsSinceSurvey, { showSurvey = it }, preferences, campaigns.isNotEmpty()
                                )
                            }
                            SwipeDir.RIGHT -> {
                                val history = if (!preferences.readHistory.contains(article.id)) preferences.readHistory + article.id else preferences.readHistory
                                onUpdatePreferences(markSeen(preferences, article.id).copy(readHistory = history))
                                cardsSincePoll++
                                if (cardsSincePoll >= 8 && feedPoll != null) showFeedPoll = true
                                proceedNext(
                                    currentIndex, filteredArticles.size, { currentIndex = it }, { cardsSinceSurvey = it },
                                    cardsSinceSurvey, { showSurvey = it }, preferences, campaigns.isNotEmpty()
                                )
                            }
                            SwipeDir.DOWN -> if (currentIndex > 0) currentIndex--
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp)
                )
        }
            val bannerText = when {
                addedWhileAway > 0 -> "Back online · $addedWhileAway new ${if (addedWhileAway == 1) "story" else "stories"} added"
                offlineNow && activeTab != "saved" && dynamicArticles.isNotEmpty() -> "You're offline · reading saved stories"
                else -> null
            }
            OfflineBanner(bannerText, online = addedWhileAway > 0, modifier = Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
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
    preferences: UserPreferences,
    hasCampaigns: Boolean = true
) {
    val nextCardsCount = cardsSinceSurvey + 1
    val now = currentTimeMillis()
    val adFreeUntilTime = preferences.adFreeUntil?.toLongOrNull() ?: 0L
    val is1HourPassActive = preferences.adFreeUntil == "AdFree" || adFreeUntilTime > now

    if (nextCardsCount >= 5 && !is1HourPassActive && hasCampaigns) {
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

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
    val glide by animateFloatAsState(progress, spring(dampingRatio = 0.9f, stiffness = 300f), label = "progress")
    LinearProgressIndicator(
        progress = { glide },
        modifier = Modifier.fillMaxWidth().height(2.dp),
        color = Primary,
        trackColor = Color.Transparent
    )
}

@Composable
fun EmptyState(activeTab: String, language: String, offline: Boolean = false, onReset: () -> Unit) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(Icons.Default.Inbox, null, tint = TextMuted, modifier = Modifier.size(48.dp))
        Text(
            text = if (offline) "You're offline and all caught up" else if (activeTab == "near_you") "All caught up on stories near you!" else "All caught up on $activeTab!",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (offline) {
            Text(
                text = "You have read every story saved on this phone. New stories will appear here as soon as you are back online.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
        if (!offline || activeTab != "for_you") {
            Button(onClick = onReset, colors = ButtonDefaults.buttonColors(containerColor = Primary)) {
                Text("Back to For You Feed")
            }
        }
    }
}

/** Remember that the user swiped past this story, so the server stops offering it (newest last, capped at 2000). */
private fun markSeen(prefs: UserPreferences, id: String): UserPreferences =
    if (prefs.seenIds.contains(id)) prefs else prefs.copy(seenIds = (prefs.seenIds + id).takeLast(2000))
