package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullView(
    article: Article,
    isBookmarked: Boolean,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit
) {
    val language = LocalAppLanguage.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
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
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, null)
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            null,
                            tint = if (isBookmarked) Primary else Color.Black
                        )
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
                .verticalScroll(scrollState)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Hero Image
            Card(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                shape = RoundedCornerShape(24.dp)
            ) {
                KamelImage(
                    resource = { asyncPainterResource(article.imageUrl) },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Headline
            Text(
                text = article.headline,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp,
                color = Color.Black
            )

            // Author & Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(32.dp).clip(CircleShape).background(Color(0xFFF5F5F5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(article.publisher.take(1), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${article.publisher} • ${article.publishedAt}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Body Text (Ollama Cleaned Content)
            Text(
                text = article.fullText, // Mapped to 'cleaned_content' in Repository
                fontSize = 16.sp,
                lineHeight = 26.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Justify
            )

            // Core Takeaways Section (Screen 7)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = LocalStrings.get("core_takeaways", language),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = Color.Gray
                )
                
                article.bullets.forEach { bullet ->
                    TakeawayBox(text = bullet)
                }
            }

            // Why It Matters (Screen 8)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = LocalStrings.get("why_it_matters", language),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Primary
                )
                Text(
                    text = article.whyItMatters,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            // Launch Full-Depth Report (Screen 8)
            TextButton(
                onClick = { /* TODO: External Link */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        LocalStrings.get("launch_full_report", language),
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TakeawayBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
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
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
