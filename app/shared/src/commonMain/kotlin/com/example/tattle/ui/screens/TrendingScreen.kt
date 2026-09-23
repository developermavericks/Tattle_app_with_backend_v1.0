package com.example.tattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.MockData
import com.example.tattle.data.sortProcessedFirst
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import com.example.tattle.ui.theme.LocalAppLanguage
import com.example.tattle.ui.theme.LocalStrings
import com.example.tattle.ui.theme.Primary
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    preferences: UserPreferences,
    onOpenArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit,
    onUpdatePreferences: (UserPreferences) -> Unit = {},
    onShareArticle: (Article) -> Unit = {}
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    val imageRepository = koinInject<ImageRepository>()
    var trendingArticles by remember { mutableStateOf(MockData.getTrendingArticles().sortProcessedFirst()) }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        val remote = articleRepository.getTrendingArticles()
        if (remote.isNotEmpty()) {
            trendingArticles = remote
        }
    }

    // Pre-fetch & decode ImageBitmaps for ALL trending articles in background
    LaunchedEffect(trendingArticles) {
        if (trendingArticles.isNotEmpty()) {
            trendingArticles.forEach { article ->
                launch {
                    imageRepository.getOrFetchBitmapForArticle(article)
                }
            }
        }
    }

    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LocalStrings.get("trending", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = textColor
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(trendingArticles) { index, article ->
                    TrendingListItem(
                        rank = index + 1,
                        article = article,
                        isDark = isDark,
                        onClick = { selectedIndex = index }
                    )
                }
            }

            // BriefView Overlay with Prev/Next Swipe & Full Depth launch button
            selectedIndex?.let { index ->
                val article = trendingArticles.getOrNull(index)
                if (article != null) {
                    BriefView(
                        article = article,
                        isBookmarked = preferences.bookmarks.contains(article.id),
                        onClose = { selectedIndex = null },
                        onToggleBookmark = {
                            val newBookmarks = if (preferences.bookmarks.contains(article.id)) {
                                preferences.bookmarks - article.id
                            } else {
                                preferences.bookmarks + article.id
                            }
                            onUpdatePreferences(preferences.copy(bookmarks = newBookmarks))
                        },
                        onShare = { onShareArticle(article) },
                        onLaunchFullText = {
                            onOpenArticle(article)
                            selectedIndex = null
                        },
                        onNextArticle = if (index < trendingArticles.size - 1) {
                            { selectedIndex = index + 1 }
                        } else null,
                        onPreviousArticle = if (index > 0) {
                            { selectedIndex = index - 1 }
                        } else null,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
fun TrendingListItem(rank: Int, article: Article, isDark: Boolean = false, onClick: () -> Unit) {
    val imageRepository = koinInject<ImageRepository>()
    var displayImageUrl by remember(article.id) {
        mutableStateOf(imageRepository.getCachedImage(article.id) ?: article.imageUrl)
    }

    LaunchedEffect(article.id) {
        val resolved = imageRepository.getOrFetchImageForArticle(article)
        displayImageUrl = resolved
    }

    val cardBg = if (isDark) DarkSurface else Color(0xFFF8F9FA)
    val headlineColor = if (isDark) DarkTextPrimary else Color.Black
    val subTextColor = if (isDark) DarkTextMuted else Color.Gray

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color(0xFF2D2D2D) else Color(0xFFEFEFEF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(if (rank <= 3) Primary else if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = if (rank <= 3) Color.White else if (isDark) DarkTextMuted else Color.DarkGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Article Details (Left/Center)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = article.category.uppercase(),
                    color = Primary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = article.headline,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    color = headlineColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = article.publisher,
                        color = headlineColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "•",
                        color = subTextColor,
                        fontSize = 10.sp
                    )
                    Text(
                        text = article.publishedAt,
                        color = subTextColor,
                        fontSize = 11.sp
                    )
                }
            }

            // Small Image Thumbnail (Right)
            Card(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEEEEEE))
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
                        modifier = Modifier.fillMaxSize().background(Color(0xFFEEEEEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
