package com.example.tattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import io.ktor.http.Url
import com.example.tattle.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedScreen(
    preferences: UserPreferences,
    onOpenArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black

    var savedArticles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(preferences.bookmarks) {
        isLoading = true
        savedArticles = articleRepository.getArticlesByIdsFromBackend(preferences.bookmarks)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LocalStrings.get("saved", language),
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
            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            } else if (savedArticles.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(Icons.Default.Bookmark, null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Text(
                        text = LocalStrings.get("no_bookmarks", language),
                        color = textColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(savedArticles) { article ->
                        SavedArticleItem(article = article, isDark = isDark, onClick = { onOpenArticle(article) })
                    }
                }
            }
        }
    }
}

@Composable
fun SavedArticleItem(article: Article, isDark: Boolean = false, onClick: () -> Unit) {
    val imageRepository = koinInject<ImageRepository>()
    var displayImageUrl by remember(article.id) {
        mutableStateOf(imageRepository.getCachedImage(article.id) ?: article.imageUrl)
    }

    LaunchedEffect(article.id) {
        val resolved = imageRepository.getOrFetchImageForArticle(article)
        displayImageUrl = resolved
    }

    val headlineColor = if (isDark) DarkTextPrimary else Color.Black
    val subTextColor = if (isDark) DarkTextMuted else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.size(100.dp),
            shape = RoundedCornerShape(12.dp)
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                article.category.uppercase(),
                color = Primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                article.headline,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                color = headlineColor
            )
            Text(
                "${article.publisher} • ${article.publishedAt}",
                color = subTextColor,
                fontSize = 12.sp
            )
        }
    }
}
