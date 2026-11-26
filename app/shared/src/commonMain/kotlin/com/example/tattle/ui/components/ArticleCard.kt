package com.example.tattle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.models.DEFAULT_BLUR_HASH
import com.example.tattle.ui.theme.*
import com.example.tattle.utils.BlurHashDecoder
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import kotlin.math.abs

enum class SwipeDir { LEFT, RIGHT, UP, DOWN }

/** What the card looked like at the moment the finger let go, so the exit animation carries on from exactly there (and at the same speed). */
data class ExitState(val dir: SwipeDir, val x: Float, val y: Float, val vx: Float, val vy: Float)

private const val CARD_CORNER = 32

/** Whole sentences that fit the card (about `maxChars` characters): never a sentence chopped in the middle. Only a single over-long sentence is cut, at a word. */
internal fun cardSummary(text: String, maxChars: Int = 210): String {
    val clean = text.trim().replace(Regex("\\s+"), " ")
    if (clean.length <= maxChars) return clean
    val sentences = clean.split(Regex("(?<=[.!?])\\s+"))
    val out = StringBuilder()
    for (sentence in sentences) {
        if (out.isEmpty()) out.append(sentence)
        else if (out.length + 1 + sentence.length <= maxChars) out.append(' ').append(sentence)
        else break
    }
    var result = out.toString()
    if (result.length > maxChars) result = result.take(maxChars).substringBeforeLast(' ').trimEnd(',', ';', ':', '-', ' ') + "\u2026"
    return result
}

// ---------------------------------------------------------------------------------------------------------------------------------
// The look of a card. No gestures here, so the same content is used for the top card, the cards waiting behind it and the one flying away.
// ---------------------------------------------------------------------------------------------------------------------------------
@Composable
fun ArticleCardContent(
    article: Article,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    parallaxX: () -> Float = { 0f },
    parallaxY: () -> Float = { 0f }
) {
    val imageRepository = koinInject<ImageRepository>()
    val haptic = LocalHapticFeedback.current

    // The picture: instant if it is already in memory, otherwise the BlurHash colours show first and the sharp picture fades in over them.
    var bitmap by remember(article.id) { mutableStateOf<ImageBitmap?>(imageRepository.peekBitmap(article.id)) }
    LaunchedEffect(article.id) {
        if (bitmap == null) bitmap = imageRepository.getOrFetchBitmapForArticle(article)
    }
    val reveal by animateFloatAsState(if (bitmap != null) 1f else 0f, tween(260), label = "reveal")
    val blur = remember(article.blurHash) {
        if (article.blurHash == DEFAULT_BLUR_HASH) null else BlurHashDecoder.decode(article.blurHash, width = 32, height = 18)
    }

    val cardBg = if (isDark) DarkSurface else Color.White
    val headlineColor = if (isDark) DarkTextPrimary else Color.Black
    val hookColor = if (isDark) DarkTextMuted else Color.DarkGray
    val timeColor = if (isDark) DarkTextMuted else Color.Gray

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(CARD_CORNER.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.35f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isDark) DarkSurfaceDim else Color(0xFFEEEEEE))
            ) {
                // the picture sits slightly enlarged and drifts against the finger while the card is dragged: depth, like on an iPhone
                Box(Modifier.fillMaxSize().graphicsLayer { scaleX = 1.08f; scaleY = 1.08f; translationX = parallaxX(); translationY = parallaxY() }) {
                    if (blur != null) {
                        Image(bitmap = blur, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Shimmer(Modifier.fillMaxSize(), isDark)
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = reveal }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
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
                    text = cardSummary(article.brief.ifBlank { article.hook }),
                    color = hookColor,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 19.sp,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.weight(1f))

                if (article.alsoReportedBy.isNotEmpty()) {
                    Text(
                        text = "Also reported by " + article.alsoReportedBy.take(3).joinToString(", "),
                        color = timeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(modifier = Modifier.size(6.dp).background(Primary, CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = article.publisher,
                            color = headlineColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = article.publishedAt, color = timeColor, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Saving: a small pop when it turns on, a gentle shrink while pressed, and a light buzz
                        val saveSource = remember { MutableInteractionSource() }
                        val savePressed by saveSource.collectIsPressedAsState()
                        val savePress by animateFloatAsState(if (savePressed) 0.84f else 1f, spring(dampingRatio = 0.6f, stiffness = 700f), label = "savePress")
                        val pop = remember { Animatable(1f) }
                        LaunchedEffect(isBookmarked) {
                            if (isBookmarked) {
                                pop.snapTo(0.55f)
                                pop.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 520f))
                            }
                        }
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onToggleBookmark()
                            },
                            interactionSource = saveSource,
                            modifier = Modifier.size(40.dp).graphicsLayer { scaleX = savePress; scaleY = savePress }
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save",
                                tint = Primary,
                                modifier = Modifier.size(24.dp).graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                            )
                        }

                        val shareSource = remember { MutableInteractionSource() }
                        val sharePressed by shareSource.collectIsPressedAsState()
                        val sharePress by animateFloatAsState(if (sharePressed) 0.84f else 1f, spring(dampingRatio = 0.6f, stiffness = 700f), label = "sharePress")
                        IconButton(
                            onClick = onShare,
                            interactionSource = shareSource,
                            modifier = Modifier.size(40.dp).graphicsLayer { scaleX = sharePress; scaleY = sharePress }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = if (isDark) DarkTextMuted else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A quietly pulsing placeholder (used until the real colours and picture are known). */
@Composable
private fun Shimmer(modifier: Modifier, isDark: Boolean) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val a = transition.animateFloat(0.45f, 1f, infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse), label = "shimmerAlpha")
    Box(modifier.graphicsLayer { alpha = a.value }.background(if (isDark) Color(0xFF3A3A3A) else Color(0xFFDCDCDC)))
}

// ---------------------------------------------------------------------------------------------------------------------------------
// The cards waiting behind the top one: the real next stories, slightly smaller and lower. They ease forward when the top card goes.
// ---------------------------------------------------------------------------------------------------------------------------------
@Composable
fun StackedCard(article: Article, depth: Int, isDark: Boolean, modifier: Modifier = Modifier) {
    val d by animateFloatAsState(depth.toFloat(), spring(dampingRatio = 0.85f, stiffness = 420f), label = "depth")
    ArticleCardContent(
        article = article,
        isBookmarked = false,
        onToggleBookmark = {},
        onShare = {},
        isDark = isDark,
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val s = 1f - 0.05f * d
                scaleX = s
                scaleY = s
                translationY = d * (size.height * 0.025f + 11.dp.toPx())          // a sliver of each waiting card peeks out under the one in front
            }
            .drawWithContent {
                drawContent()
                drawRoundRect(Color.Black.copy(alpha = 0.05f * d), cornerRadius = CornerRadius(CARD_CORNER.dp.toPx()))
            }
    )
}

// ---------------------------------------------------------------------------------------------------------------------------------
// The top card: follows the finger on ONE axis, flings away by distance or speed, springs back otherwise. Nothing in here recomposes while
// dragging: positions are read only when drawing.
// ---------------------------------------------------------------------------------------------------------------------------------
@Composable
fun SwipeableArticleCard(
    article: Article,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onOpenBrief: () -> Unit,
    onOpenFull: () -> Unit,
    canGoBack: Boolean,
    canAdvance: Boolean,
    onCommit: (SwipeDir, ExitState) -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    var widthPx by remember { mutableFloatStateOf(1f) }
    var heightPx by remember { mutableFloatStateOf(1f) }
    var dragX by remember(article.id) { mutableFloatStateOf(0f) }
    var dragY by remember(article.id) { mutableFloatStateOf(0f) }
    var horizontal by remember(article.id) { mutableStateOf(true) }
    val settle = remember(article.id) { arrayOfNulls<Job>(1) }

    // Arrives from the "waiting behind" position instead of popping in
    val enter = remember(article.id) { Animatable(1f) }
    LaunchedEffect(article.id) { enter.animateTo(0f, spring(dampingRatio = 0.82f, stiffness = 380f)) }

    fun snapBack() {
        settle[0]?.cancel()
        settle[0] = scope.launch {
            val x0 = dragX
            val y0 = dragY
            launch { animate(x0, 0f, animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f)) { v, _ -> dragX = v } }
            animate(y0, 0f, animationSpec = spring(dampingRatio = 0.72f, stiffness = 520f)) { v, _ -> dragY = v }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { widthPx = it.width.toFloat().coerceAtLeast(1f); heightPx = it.height.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                val e = enter.value
                val s = (1f - 0.05f * e) * (1f - ((abs(dragX) + abs(dragY)) / 2400f).coerceAtMost(0.06f))
                scaleX = s
                scaleY = s
                translationX = dragX
                translationY = dragY + e * (size.height * 0.025f + 11.dp.toPx())
                rotationZ = if (horizontal) dragX * 0.03f else 0f
            }
            .pointerInput(article.id, canGoBack, canAdvance) {
                val slop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    settle[0]?.cancel()
                    val tracker = VelocityTracker()
                    tracker.addPosition(down.uptimeMillis, down.position)
                    var axis = 0                      // 0 = undecided, 1 = left/right, 2 = up/down
                    var travelX = 0f
                    var travelY = 0f
                    var pastLine = false
                    val completed = drag(down.id) { change ->
                        tracker.addPosition(change.uptimeMillis, change.position)
                        val d = change.positionChange()
                        travelX += d.x
                        travelY += d.y
                        if (axis == 0 && (abs(travelX) > slop || abs(travelY) > slop)) {
                            axis = if (abs(travelY) > abs(travelX)) 2 else 1
                            horizontal = axis == 1
                        }
                        if (axis != 0) {
                            change.consume()
                            if (axis == 2) {
                                dragX = 0f
                                val ny = dragY + d.y
                                // the first card cannot go back: pulling down meets a rubber band (a few millimetres) that springs back
                                dragY = if (canGoBack) ny
                                else if (ny > 0f) (dragY.coerceAtLeast(0f) + d.y.coerceAtLeast(0f) * 0.25f).coerceAtMost(heightPx * 0.025f)
                                else ny
                            } else {
                                dragY = 0f
                                dragX += d.x
                            }
                            val over = if (axis == 1) abs(dragX) > widthPx * 0.28f
                            else (dragY < -heightPx * 0.16f || (canGoBack && dragY > heightPx * 0.16f))
                            if (over && !pastLine) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            pastLine = over
                        }
                    }
                    if (axis == 0) return@awaitEachGesture                              // it was a tap or a long press: the other detector handles it
                    val v = tracker.calculateVelocity()
                    val dir: SwipeDir? = if (!completed) null else when {
                        axis == 1 && (abs(dragX) > widthPx * 0.28f || (abs(v.x) > 1800f && v.x * dragX > 0f)) -> if (dragX > 0) SwipeDir.RIGHT else SwipeDir.LEFT
                        axis == 2 && (dragY < -heightPx * 0.16f || (v.y < -1500f && dragY < 0f)) -> SwipeDir.UP
                        axis == 2 && canGoBack && (dragY > heightPx * 0.16f || (v.y > 1500f && dragY > 0f)) -> SwipeDir.DOWN
                        else -> null
                    }
                    if (dir != null && (dir == SwipeDir.DOWN || canAdvance)) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val exit = ExitState(dir, dragX, dragY, v.x, v.y)
                        dragX = 0f
                        dragY = 0f
                        onCommit(dir, exit)                                             // the next card takes over at once; a copy of this one flies away
                    } else {
                        snapBack()
                    }
                }
            }
            .pointerInput(article.id) {
                detectTapGestures(onTap = { onOpenBrief() }, onLongPress = { onOpenFull() })
            }
            .drawWithContent {
                drawContent()
                // a soft green / red wash while a left/right swipe is past the line, drawn without recomposing
                val p = if (horizontal) abs(dragX) / (size.width * 0.6f) else 0f
                if (p > 0.2f) {
                    drawRoundRect(
                        (if (dragX > 0) Color(0xFF2E7D32) else Color(0xFFC62828)).copy(alpha = ((p - 0.2f) * 0.35f).coerceIn(0f, 0.18f)),
                        cornerRadius = CornerRadius(CARD_CORNER.dp.toPx())
                    )
                }
            }
    ) {
        ArticleCardContent(
            article = article,
            isBookmarked = isBookmarked,
            onToggleBookmark = onToggleBookmark,
            onShare = onShare,
            isDark = isDark,
            modifier = Modifier.fillMaxSize(),
            parallaxX = { -dragX * 0.07f },
            parallaxY = { -dragY * 0.05f }
        )

        // the icons that grow as a swipe gets closer to counting
        SwipeBadge(Color(0xFF1B5E20), Icons.Default.Check, Modifier.align(Alignment.Center)) { if (horizontal) (dragX / (widthPx * 0.5f)).coerceIn(0f, 1f) else 0f }
        SwipeBadge(Color(0xFFB71C1C), Icons.Default.Close, Modifier.align(Alignment.Center)) { if (horizontal) (-dragX / (widthPx * 0.5f)).coerceIn(0f, 1f) else 0f }
        SwipeBadge(Primary, Icons.Default.KeyboardArrowUp, Modifier.align(Alignment.Center)) { if (!horizontal) (-dragY / (heightPx * 0.3f)).coerceIn(0f, 1f) else 0f }
    }
}

@Composable
private fun SwipeBadge(color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, progress: () -> Float) {
    Box(
        modifier = modifier
            .graphicsLayer {
                val a = progress()
                alpha = if (a > 0.1f) a else 0f
                val s = 0.6f + a * 0.9f
                scaleX = s
                scaleY = s
            }
            .background(color.copy(alpha = 0.9f), CircleShape)
            .padding(24.dp)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(64.dp))
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
// The copy of a swiped card that flies off the screen while the next card is already in place.
// ---------------------------------------------------------------------------------------------------------------------------------
@Composable
fun ExitingCard(
    article: Article,
    isBookmarked: Boolean,
    isDark: Boolean,
    exit: ExitState,
    widthPx: Float,
    heightPx: Float,
    onDone: () -> Unit
) {
    val x = remember { Animatable(exit.x) }
    val y = remember { Animatable(exit.y) }
    val fade = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        val tx = when (exit.dir) { SwipeDir.LEFT -> -widthPx * 1.3f; SwipeDir.RIGHT -> widthPx * 1.3f; else -> exit.x }
        val ty = when (exit.dir) { SwipeDir.UP -> -heightPx * 1.15f; SwipeDir.DOWN -> heightPx * 1.15f; else -> exit.y }
        coroutineScope {
            launch { x.animateTo(tx, tween(230, easing = FastOutLinearInEasing)) }
            launch { y.animateTo(ty, tween(230, easing = FastOutLinearInEasing)) }
            launch { fade.animateTo(0f, tween(230)) }
        }
        onDone()
    }
    val sideways = exit.dir == SwipeDir.LEFT || exit.dir == SwipeDir.RIGHT
    ArticleCardContent(
        article = article,
        isBookmarked = isBookmarked,
        onToggleBookmark = {},
        onShare = {},
        isDark = isDark,
        modifier = Modifier.fillMaxSize().graphicsLayer {
            translationX = x.value
            translationY = y.value
            rotationZ = if (sideways) x.value * 0.03f else 0f
            alpha = 0.3f + 0.7f * fade.value
        }
    )
}
