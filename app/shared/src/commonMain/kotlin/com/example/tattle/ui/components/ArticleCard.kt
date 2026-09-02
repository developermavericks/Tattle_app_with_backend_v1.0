package com.example.tattle.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.*
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs

@Composable
fun ArticleCard(
    article: Article,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onOpenBrief: () -> Unit,
    onOpenFull: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onReaction: (String) -> Unit,
    onImageLoaded: (String) -> Unit,
    currentReaction: String?,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    val imageRepository = koinInject<ImageRepository>()
    var pixabayImageUrl by remember(article.id) { mutableStateOf<String?>(null) }
    val isExpiredPixabayUrl = article.imageUrl.contains("pixabay.com/get/") || article.imageUrl.isBlank()

    LaunchedEffect(article.id) {
        if (isExpiredPixabayUrl || pixabayImageUrl == null) {
            val headlineKeywords = article.headline
                .split(" ")
                .filter { it.length > 3 }
                .take(2)
                .joinToString(" ")
            
            val primaryQuery = "${article.category} $headlineKeywords"
            var url = imageRepository.searchImage(primaryQuery)
            
            if (url == null) {
                url = imageRepository.searchImage(article.category)
            }
            
            if (url != null) {
                pixabayImageUrl = url
                onImageLoaded(url)
            } else {
                pixabayImageUrl = "https://picsum.photos/seed/${article.id}/800/1000"
            }
        }
    }

    var showReactions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    // Color animation based on swipe
    val swipeProgress = (offsetX.value / 400f).coerceIn(-1f, 1f)
    val cardColor by animateColorAsState(
        targetValue = when {
            swipeProgress > 0.05f -> Color(0xFF1B5E20).copy(alpha = (abs(swipeProgress) * 1.2f).coerceIn(0.1f, 0.95f))
            swipeProgress < -0.05f -> Color(0xFFB71C1C).copy(alpha = (abs(swipeProgress) * 1.2f).coerceIn(0.1f, 0.95f))
            else -> Color(0xFFF5F5F5)
        },
        animationSpec = tween(150)
    )

    Card(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .pointerInput(article.id) {
                detectDragGestures(
                    onDragCancel = {
                        scope.launch {
                            launch { offsetX.animateTo(0f) }
                            launch { rotation.animateTo(0f) }
                            launch { scale.animateTo(1f) }
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value > 300f) {
                            scope.launch {
                                offsetX.animateTo(1000f)
                                onSwipeRight()
                            }
                        } else if (offsetX.value < -300f) {
                            scope.launch {
                                offsetX.animateTo(-1000f)
                                onSwipeLeft()
                            }
                        } else {
                            scope.launch {
                                launch { offsetX.animateTo(0f) }
                                launch { rotation.animateTo(0f) }
                                launch { scale.animateTo(1f) }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            rotation.snapTo(offsetX.value * 0.04f)
                            scale.snapTo(1f - (abs(offsetX.value) / 2000f).coerceAtMost(0.1f))
                        }
                    }
                )
            }
            .pointerInput(article.id) {
                detectTapGestures(
                    onDoubleTap = { showReactions = true },
                    onTap = { onOpenBrief() },
                    onLongPress = { onOpenFull() }
                )
            },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Image Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.LightGray)
                ) {
                    val finalImageUrl = pixabayImageUrl ?: if (!isExpiredPixabayUrl) article.imageUrl else "https://picsum.photos/seed/${article.id}/800/1000"
                    
                    KamelImage(
                        resource = { asyncPainterResource(finalImageUrl) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { progress -> CircularProgressIndicator(progress = { progress }, color = Primary, modifier = Modifier.align(Alignment.Center).size(24.dp)) },
                        onFailure = {
                            KamelImage(
                                resource = { asyncPainterResource("https://picsum.photos/seed/${article.id}/800/1000") },
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    )

                    // Overlay Tags
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Primary, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = LocalStrings.get("trending", language),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Primary.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = article.category,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = article.headline,
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.hook,
                        color = Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = article.publisher,
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = article.publishedAt,
                                color = Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onToggleBookmark, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = onShare, modifier = Modifier.size(40.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Swipe Overlays
            val overlayAlphaRight = (offsetX.value / 300f).coerceIn(0f, 1f)
            val overlayAlphaLeft = (-offsetX.value / 300f).coerceIn(0f, 1f)

            if (overlayAlphaRight > 0.1f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = overlayAlphaRight, scaleX = overlayAlphaRight * 1.5f, scaleY = overlayAlphaRight * 1.5f)
                        .background(Color(0xFF1B5E20).copy(alpha = 0.9f), CircleShape)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }

            if (overlayAlphaLeft > 0.1f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = overlayAlphaLeft, scaleX = overlayAlphaLeft * 1.5f, scaleY = overlayAlphaLeft * 1.5f)
                        .background(Color(0xFFB71C1C).copy(alpha = 0.9f), CircleShape)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }

            // Reactions Overlay
            if (showReactions) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable { showReactions = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 64.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("❤️", "😂", "😮", "🤯", "😡").forEach { emoji ->
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .clickable {
                                            onReaction(emoji)
                                            showReactions = false
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
