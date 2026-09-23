package com.example.tattle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

data class SectorDisplayItem(val slug: String, val icon: String, val name: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    preferences: UserPreferences,
    onCategoryClick: (String) -> Unit,
    onOpenArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit
) {
    val language = LocalAppLanguage.current
    val articleRepository = koinInject<ArticleRepository>()
    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black

    val defaultSectorItems = remember {
        listOf(
            SectorDisplayItem("ai", "🤖", "AI & Robotics"),
            SectorDisplayItem("climate and environment", "🌍", "Climate & Environment"),
            SectorDisplayItem("creator economy", "🤳", "Creator Economy"),
            SectorDisplayItem("education", "📚", "Education"),
            SectorDisplayItem("gaming", "🎮", "Gaming"),
            SectorDisplayItem("geopolitics", "🌐", "Geopolitics"),
            SectorDisplayItem("healthcare", "🏥", "Healthcare"),
            SectorDisplayItem("lifestyle", "✨", "Lifestyle"),
            SectorDisplayItem("media and entertainment", "🎬", "Entertainment"),
            SectorDisplayItem("money and business", "💰", "Business & Money"),
            SectorDisplayItem("pop culture", "🎶", "Pop Culture"),
            SectorDisplayItem("science and space", "🪐", "Science & Space"),
            SectorDisplayItem("sports", "⚽", "Sports"),
            SectorDisplayItem("startups", "🚀", "Startups"),
            SectorDisplayItem("tech", "💻", "Tech"),
            SectorDisplayItem("world news", "📰", "World News")
        )
    }

    var availableSectors by remember { mutableStateOf<List<SectorDisplayItem>>(defaultSectorItems) }
    val imageRepository = koinInject<ImageRepository>()

    LaunchedEffect(Unit) {
        val serverSectors = articleRepository.getSectors()
        if (serverSectors.isNotEmpty()) {
            val mapped = serverSectors.map { slug ->
                val match = defaultSectorItems.find { it.slug == slug }
                match ?: SectorDisplayItem(slug, "📰", slug.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
            }
            availableSectors = mapped.distinctBy { it.slug }
        }
    }

    // Background pre-fetcher for Explore Screen top sectors & photos
    LaunchedEffect(availableSectors) {
        val topSectors = availableSectors.take(6).map { it.slug }
        topSectors.forEach { sector ->
            launch {
                val articles = articleRepository.getArticlesBySector(sector)
                articles.take(3).forEach { article ->
                    imageRepository.getOrFetchBitmapForArticle(article)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LocalStrings.get("explore", language),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                LocalStrings.get("categories_label", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = textColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(availableSectors) { item ->
                    SectorCard(
                        item = item,
                        isDark = isDark,
                        onClick = { onCategoryClick(item.slug) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectorCard(item: SectorDisplayItem, isDark: Boolean = false, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkSurface else Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.icon,
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                text = item.name,
                color = if (isDark) DarkTextPrimary else Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}
