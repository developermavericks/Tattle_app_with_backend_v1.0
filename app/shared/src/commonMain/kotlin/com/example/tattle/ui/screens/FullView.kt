package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

enum class ReaderMode {
    BRIEF, IN_DEPTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullView(
    article: Article,
    isBookmarked: Boolean,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    isDark: Boolean = false,
    userIdentifier: String = "default_user",
    initialMode: ReaderMode = ReaderMode.IN_DEPTH
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    val imageRepository = koinInject<ImageRepository>()
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var displayImageUrl by remember(article.id) {
        mutableStateOf(imageRepository.getCachedImage(article.id) ?: article.imageUrl)
    }

    LaunchedEffect(article.id) {
        val resolved = imageRepository.getOrFetchImageForArticle(article)
        displayImageUrl = resolved
    }

    var readerMode by remember(initialMode) { mutableStateOf(initialMode) }
    var likesCount by remember { mutableStateOf(article.likes) }
    var isLiked by remember { mutableStateOf(false) }

    val bgColor = if (isDark) DarkBackground else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black
    val subTextColor = if (isDark) DarkTextMuted else Color.Gray
    val bodyTextColor = if (isDark) DarkTextMuted else Color.DarkGray
    val cardBoxBgColor = if (isDark) DarkSurfaceDim else Color(0xFFF9F9F9)

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(bgColor)) {
                TopAppBar(
                    title = {
                        Text(
                            text = article.category.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Primary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                articleRepository.recordShareOnServer(userIdentifier, article.id)
                            }
                            onShare()
                        }) {
                            Icon(Icons.Default.Share, null, tint = textColor)
                        }
                        IconButton(onClick = {
                            coroutineScope.launch {
                                articleRepository.toggleBookmarkOnServer(userIdentifier, article.id)
                            }
                            onToggleBookmark()
                        }) {
                            Icon(
                                if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                null,
                                tint = if (isBookmarked) Primary else textColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )

                // Reader Mode Segmented Control Bar (Seamless Brief <-> In-Depth Navigation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                        .background(if (isDark) DarkSurface else Color(0xFFF2F2F2), RoundedCornerShape(20.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (readerMode == ReaderMode.BRIEF) Primary else Color.Transparent)
                            .clickable { readerMode = ReaderMode.BRIEF }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ BRIEF",
                            color = if (readerMode == ReaderMode.BRIEF) Color.White else subTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (readerMode == ReaderMode.IN_DEPTH) Primary else Color.Transparent)
                            .clickable { readerMode = ReaderMode.IN_DEPTH }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📖 IN-DEPTH",
                            color = if (readerMode == ReaderMode.IN_DEPTH) Color.White else subTextColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero Image
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
                shape = RoundedCornerShape(20.dp)
            ) {
                var imageBitmap by remember(article.id) { mutableStateOf<ImageBitmap?>(null) }

                LaunchedEffect(article.id) {
                    imageBitmap = imageRepository.getOrFetchBitmapForArticle(article)
                }

                if (imageBitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap!!,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize().background(if (isDark) DarkSurfaceDim else Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Headline
            Text(
                text = article.headline,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 30.sp,
                color = textColor
            )

            // Author, Publisher & Date
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isDark) DarkSurfaceDim else Color(0xFFF5F5F5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(article.publisher.take(1), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${article.publisher} • ${article.publishedAt}",
                        color = subTextColor,
                        fontSize = 12.sp
                    )
                }

                // Like Button connected to Backend
                IconButton(onClick = {
                    coroutineScope.launch {
                        val newLikes = articleRepository.toggleLikeOnServer(userIdentifier, article.id)
                        if (newLikes != null) {
                            likesCount = newLikes
                            isLiked = !isLiked
                        } else {
                            isLiked = !isLiked
                            likesCount += if (isLiked) 1 else -1
                        }
                    }
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Primary else subTextColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "$likesCount", fontSize = 12.sp, color = subTextColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            HorizontalDivider(color = if (isDark) Color(0xFF2C2C2C) else Color(0xFFEEEEEE))

            // Animated Switch between Brief Mode and In-Depth Mode
            AnimatedContent(
                targetState = readerMode,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { mode ->
                when (mode) {
                    ReaderMode.BRIEF -> {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // High Impact Hook
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBoxBgColor, RoundedCornerShape(16.dp))
                                    .padding(18.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = "⚡ EXECUTIVE BRIEF",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Primary,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = article.hook,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        color = textColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Core Takeaways Section
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = LocalStrings.get("core_takeaways", language),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = subTextColor
                                )
                                article.bullets.forEach { bullet ->
                                    TakeawayBox(text = bullet, isDark = isDark)
                                }
                            }

                            // Why It Matters
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(cardBoxBgColor, RoundedCornerShape(16.dp))
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = LocalStrings.get("why_it_matters", language),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Primary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = article.whyItMatters,
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    ReaderMode.IN_DEPTH -> {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Full Text Body
                            Text(
                                text = article.fullText,
                                fontSize = 16.sp,
                                lineHeight = 26.sp,
                                color = bodyTextColor,
                                textAlign = TextAlign.Justify
                            )

                            // Core Takeaways Section
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "ANALYSIS HIGHLIGHTS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = subTextColor
                                )
                                article.bullets.forEach { bullet ->
                                    TakeawayBox(text = bullet, isDark = isDark)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TakeawayBox(text: String, isDark: Boolean = false) {
    val cardBgColor = if (isDark) DarkSurface else Color(0xFFF5F5F5)
    val textColor = if (isDark) DarkTextPrimary else Color.Black
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBgColor, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .padding(top = 6.dp)
                    .background(Primary, CircleShape)
            )
            Text(
                text = text,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
