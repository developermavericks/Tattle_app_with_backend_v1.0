package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.*

@Composable
fun BriefView(
    article: Article,
    isBookmarked: Boolean,
    onClose: () -> Unit,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onLaunchFullText: () -> Unit,
    onNextArticle: (() -> Unit)? = null,
    onPreviousArticle: (() -> Unit)? = null,
    isDark: Boolean = false
) {
    val language = LocalAppLanguage.current
    val sheetBgColor = if (isDark) DarkSurface else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black
    val cardBgColor = if (isDark) DarkSurfaceDim else Color(0xFFF5F5F5)

    var dragOffsetX by remember(article.id) { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(sheetBgColor)
                .clickable(enabled = false) {}
                .then(
                    if (onNextArticle != null || onPreviousArticle != null) {
                        Modifier.pointerInput(article.id) {
                            detectHorizontalDragGestures(
                                onDragStart = { dragOffsetX = 0f },
                                onDragEnd = {
                                    if (dragOffsetX < -100f && onNextArticle != null) {
                                        onNextArticle()
                                    } else if (dragOffsetX > 100f && onPreviousArticle != null) {
                                        onPreviousArticle()
                                    }
                                    dragOffsetX = 0f
                                },
                                onDragCancel = { dragOffsetX = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffsetX += dragAmount
                                }
                            )
                        }
                    } else Modifier
                )
                .padding(24.dp)
        ) {
            // Header with Tag, Navigation Chevrons, Bookmark, Share and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = LocalStrings.get("trending", language),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Primary, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = article.category,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = onToggleBookmark, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = if (isDark) DarkTextMuted else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (onPreviousArticle != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
                                .clickable { onPreviousArticle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronLeft, "Previous Article", tint = textColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    if (onNextArticle != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF333333) else Color(0xFFEEEEEE))
                                .clickable { onNextArticle() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ChevronRight, "Next Article", tint = textColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Primary)
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, "Close", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (onNextArticle != null || onPreviousArticle != null) {
                Text(
                    text = "👈 Swipe right/left or tap arrows to switch trending stories 👉",
                    color = if (isDark) DarkTextMuted else Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp)
                )
            }

            AnimatedContent(
                targetState = article,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(300)) + slideInHorizontally(initialOffsetX = { 300 }))
                        .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutHorizontally(targetOffsetX = { -300 }))
                },
                label = "BriefArticleTransition",
                modifier = Modifier.weight(1f)
            ) { targetArticle ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    // Headline
                    Text(
                        text = targetArticle.headline,
                        color = textColor,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Summary
                    Text(
                        text = targetArticle.brief,
                        color = textColor,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 25.sp
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Key Takeaways
                    Text(
                        text = LocalStrings.get("core_takeaways", language),
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    targetArticle.bullets.forEach { bullet ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBgColor)
                        ) {
                            Text(
                                text = bullet,
                                color = textColor,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Why It Matters
                    Text(
                        text = LocalStrings.get("why_it_matters", language),
                        color = Primary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBgColor)
                    ) {
                        Text(
                            text = targetArticle.whyItMatters,
                            color = textColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Launch Full Depth Button at Bottom
                    Button(
                        onClick = { onLaunchFullText() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = LocalStrings.get("launch_full_report", language),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.OpenInNew, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
