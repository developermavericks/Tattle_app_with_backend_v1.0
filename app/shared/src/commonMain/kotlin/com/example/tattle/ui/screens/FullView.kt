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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

@Composable
fun FullView(
    article: Article,
    isBookmarked: Boolean,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onReaction: (String) -> Unit = {},
    currentReaction: String? = null,
    nextArticle: Article? = null,
    onLoadNextArticle: (Article) -> Unit = {}
) {
    val language = LocalAppLanguage.current
    val scrollState = rememberScrollState()
    val scrollProgress = if (scrollState.maxValue > 0) scrollState.value.toFloat() / scrollState.maxValue else 0f
    var isFollowingThread by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(Background.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onClose) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextMuted)
                        }
                        Column {
                            Text(
                                text = LocalStrings.get("exclusive", language),
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = article.headline,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                        }
                        IconButton(onClick = onToggleBookmark) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (isBookmarked) Secondary else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Progress Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(scrollProgress)
                            .background(Primary)
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Category & Time
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "#${article.category.uppercase()}",
                            color = Secondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "• ${article.readTime}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Headline
                Text(
                    text = article.headline,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 36.sp
                )

                // Author
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Surface)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AT", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "Alex Thorne", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "MAR 14, 2026 • TOKYO HUB",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Hero Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                ) {
                    KamelImage(
                        resource = { asyncPainterResource(article.imageUrl) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { progress -> CircularProgressIndicator(progress = { progress }, color = Primary, modifier = Modifier.align(Alignment.Center).size(24.dp)) },
                        onFailure = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, null, tint = Color.Gray) } }
                    )
                }

                // Body Text
                article.fullText.split("\n\n").forEachIndexed { index, para ->
                    Text(
                        text = para,
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Justify
                    )

                    if (index == 2) {
                        // Pull Quote
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Primary.copy(alpha = 0.05f))
                                .border(
                                    width = 1.dp,
                                    brush = Brush.horizontalGradient(listOf(Primary, Color.Transparent)),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "\"The noise is the message. The speed is the verification. If you're not scrolling, you're becoming obsolete.\"",
                                color = Primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Tags
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("#cybernetics", "#media-theory", "#gen-z", "#breaking").forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(50))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = tag, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Stats & Reaction
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Whatshot, null, tint = Secondary, modifier = Modifier.size(16.dp))
                                Text(text = "${article.likes}", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Message, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                Text(text = "${article.commentsCount}", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { isFollowingThread = !isFollowingThread },
                            modifier = Modifier.height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowingThread) Secondary else Primary.copy(alpha = 0.2f),
                                contentColor = if (isFollowingThread) Color.Black else Primary
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = if (!isFollowingThread) androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f)) else null
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (isFollowingThread) Icons.Default.Check else Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isFollowingThread) LocalStrings.get("following", language) else LocalStrings.get("follow_thread", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }

                // Up Next
                if (nextArticle != null) {
                    Column(modifier = Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = LocalStrings.get("up_next", language),
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Card(
                            onClick = { onLoadNextArticle(nextArticle) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.5f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Surface)
                                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                ) {
                                    KamelImage(
                                        resource = { asyncPainterResource(nextArticle.imageUrl) },
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        onLoading = { progress -> CircularProgressIndicator(progress = { progress }, color = Primary, modifier = Modifier.align(Alignment.Center).size(16.dp)) },
                                        onFailure = { Box(modifier = Modifier.fillMaxSize().background(Color.LightGray), contentAlignment = Alignment.Center) { Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp)) } }
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "${nextArticle.category.uppercase()} • ${nextArticle.readTime.uppercase()}",
                                        color = Primary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = nextArticle.headline,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        lineHeight = 16.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
