package com.example.tattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.MockData
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
fun ExploreScreen(
    preferences: UserPreferences,
    onCategoryClick: (String) -> Unit,
    onOpenArticle: (Article) -> Unit,
    onOpenSettings: () -> Unit
) {
    val language = LocalAppLanguage.current
    val imageRepository = koinInject<ImageRepository>()
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = listOf(
        "ai", "climate and environment", "creator economy", "education",
        "gaming", "geopolitics", "healthcare", "lifestyle",
        "media and entertainment", "money and business", "pop culture",
        "science and space", "sports", "startups", "tech", "world news"
    )

    fun getDisplayName(slug: String): String = when (slug.lowercase()) {
        "ai" -> "AI & Robotics"
        "climate and environment" -> "Climate & Environment"
        "creator economy" -> "Creator Economy"
        "education" -> "Education"
        "gaming" -> "Gaming"
        "geopolitics" -> "Geopolitics"
        "healthcare" -> "Healthcare"
        "lifestyle" -> "Lifestyle"
        "media and entertainment" -> "Entertainment"
        "money and business" -> "Business & Money"
        "pop culture" -> "Pop Culture"
        "science and space" -> "Science & Space"
        "sports" -> "Sports"
        "startups" -> "Startups"
        "tech" -> "Tech"
        "world news" -> "World News"
        else -> slug.replaceFirstChar { it.uppercase() }
    }

    // Search Results
    var searchResults by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery) {
        if (searchQuery.length > 2) {
            isSearching = true
            searchResults = imageRepository.searchImages(searchQuery)
            isSearching = false
        } else {
            searchResults = emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        LocalStrings.get("explore", language),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                placeholder = { Text(LocalStrings.get("search_hint", language)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (isSearching) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            if (searchQuery.length > 2) {
                Text(
                    "Search Results",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(searchResults) { imageUrl ->
                        Card(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            KamelImage(
                                resource = { asyncPainterResource(imageUrl) },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            } else {
                Text(
                    LocalStrings.get("categories_label", language),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(categories.chunked(2)) { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { category ->
                                CategoryCard(
                                    name = getDisplayName(category),
                                    modifier = Modifier.weight(1f),
                                    onClick = { onCategoryClick(category) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(name: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
        }
    }
}
