package com.example.tattle.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.LocalAppLanguage
import com.example.tattle.ui.theme.LocalStrings
import com.example.tattle.ui.theme.Primary
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    preferences: UserPreferences,
    onOpenArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    var trendingArticles by remember { mutableStateOf<List<Article>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        trendingArticles = articleRepository.getTrendingArticles()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LocalStrings.get("trending", language),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Primary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(trendingArticles) { article ->
                        TrendingArticleItem(article = article, onClick = { onOpenArticle(article) })
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingArticleItem(article: Article, onClick: () -> Unit) {
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
            KamelImage(
                resource = asyncPainterResource(article.imageUrl),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
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
                lineHeight = 20.sp
            )
            Text(
                "${article.publisher} • ${article.publishedAt}",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
