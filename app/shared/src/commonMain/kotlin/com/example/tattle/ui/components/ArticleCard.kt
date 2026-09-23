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
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
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
import com.example.tattle.utils.BlurHashDecoder
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.http.Url
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
    onSwipeUp: () -> Unit = {},
    onReaction: (String) -> Unit,
    onImageLoaded: (String) -> Unit,
    currentReaction: String?,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val imageRepository = koinInject<ImageRepository>()
    var displayImageUrl by remember(article.id) {
        mutableStateOf(imageRepository.getCachedImage(article.id) ?: article.imageUrl)
    }

    LaunchedEffect(article.id) {
        val resolved = imageRepository.getOrFetchImageForArticle(article)
        displayImageUrl = resolved
        onImageLoaded(resolved)
    }

    var showReactions by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }

    val defaultCardBg = if (isDark) DarkSurface else Color.White
    val headlineColor = if (isDark) DarkTextPrimary else Color.Black
    val hookColor = if (isDark) DarkTextMuted else Color.DarkGray
    val publisherColor = if (isDark) DarkTextPrimary else Color.Black
    val timeColor = if (isDark) DarkTextMuted else Color.Gray

    // Color animation based on swipe
    val swipeProgress = (offsetX.value / 400f).coerceIn(-1f, 1f)
    val cardColor by animateColorAsState(
        targetValue = when {
            swipeProgress > 0.20f -> Color(0xFFE8F5E9)
            swipeProgress < -0.20f -> Color(0xFFFFEBEE)
            else -> defaultCardBg
        },
        animationSpec = tween(150)
    )

    Card(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                translationY = offsetY.value
                rotationZ = rotation.value
                scaleX = scale.value
                scaleY = scale.value
            }
            .pointerInput(article.id) {
                detectDragGestures(
                    onDragCancel = {
                        scope.launch {
                            launch { offsetX.animateTo(0f) }
                            launch { offsetY.animateTo(0f) }
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
                        } else if (offsetY.value < -250f) {
                            scope.launch {
                                launch { rotation.animateTo(0f) }
                                launch { offsetX.animateTo(0f) }
                                launch { offsetY.animateTo(-1200f) }
                                onSwipeUp()
                            }
                        } else {
                            scope.launch {
                                launch { offsetX.animateTo(0f) }
                                launch { offsetY.animateTo(0f) }
                                launch { rotation.animateTo(0f) }
                                launch { scale.animateTo(1f) }
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newX = offsetX.value + dragAmount.x
                            val newY = offsetY.value + dragAmount.y
                            offsetX.snapTo(newX)
                            offsetY.snapTo(newY)

                            // Keep card straight (0° tilt) when dragging vertically upwards to skip
                            val isVerticalUp = abs(newY) > abs(newX) && newY < 0f
                            val targetRotation = if (isVerticalUp) 0f else newX * 0.04f
                            rotation.snapTo(targetRotation)

                            scale.snapTo(1f - ((abs(newX) + abs(newY)) / 2000f).coerceAtMost(0.1f))
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
                        .weight(1.35f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) DarkSurfaceDim else Color.LightGray)
                ) {
                    // Instant 0ms BlurHash Color Placeholder Preview
                    val blurHashBitmap = remember(article.blurHash) {
                        BlurHashDecoder.decode(article.blurHash, width = 32, height = 18)
                    }

                    if (blurHashBitmap != null) {
                        Image(
                            bitmap = blurHashBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    var imageBitmap by remember(article.id) { mutableStateOf<ImageBitmap?>(null) }

                    LaunchedEffect(article.id) {
                        imageBitmap = imageRepository.getOrFetchBitmapForArticle(article)
                        imageRepository.getCachedImage(article.id)?.let { onImageLoaded(it) }
                    }

                    if (imageBitmap != null) {
                        Image(
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

                // Info Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = article.headline,
                        color = headlineColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = article.hook,
                        color = hookColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 17.sp,
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
                                color = publisherColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = article.publishedAt,
                                color = timeColor,
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
                                    tint = if (isDark) DarkTextMuted else Color.Gray,
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
            val overlayAlphaUp = (-offsetY.value / 250f).coerceIn(0f, 1f)

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

            if (overlayAlphaUp > 0.1f && abs(offsetX.value) < abs(offsetY.value)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(alpha = overlayAlphaUp, scaleX = overlayAlphaUp * 1.5f, scaleY = overlayAlphaUp * 1.5f)
                        .background(Primary.copy(alpha = 0.9f), CircleShape)
                        .padding(24.dp)
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White, modifier = Modifier.size(64.dp))
                }
            }
        }
    }
}
