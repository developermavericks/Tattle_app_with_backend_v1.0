package com.example.tattle.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/** A dialog or card that grows in with a small spring (scale 0.92 -> 1 while fading in), like an iPhone sheet. */
@Composable
fun Modifier.popIn(): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) { a.animateTo(1f, spring(dampingRatio = 0.8f, stiffness = 380f)) }
    return this.graphicsLayer {
        val v = a.value
        alpha = v.coerceIn(0f, 1f)
        val s = 0.92f + 0.08f * v
        scaleX = s
        scaleY = s
    }
}

/** Content that rises 14 dp and fades in after `delayMs` (used to stagger chips and lists). */
@Composable
fun Appear(delayMs: Int, content: @Composable () -> Unit) {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        a.animateTo(1f, spring(dampingRatio = 0.85f, stiffness = 400f))
    }
    Box(Modifier.graphicsLayer { alpha = a.value.coerceIn(0f, 1f); translationY = (1f - a.value) * 14.dp.toPx() }) { content() }
}

/** iOS-style press: the element shrinks a little under the finger and springs back with a small bounce. It does not take the touch, so clicks still work. */
fun Modifier.pressScale(pressedScale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, spring(dampingRatio = 0.55f, stiffness = 650f), label = "pressScale")
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

/** Fades and slides up when it first appears, one after another (index 0, 1, 2 ...). */
fun Modifier.appearOnEnter(index: Int = 0): Modifier = composed {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 55L); shown = true }
    val progress by animateFloatAsState(if (shown) 1f else 0f, tween(420, easing = FastOutSlowInEasing), label = "appear")
    this.graphicsLayer { alpha = progress; translationY = (1f - progress) * 48f }
}
