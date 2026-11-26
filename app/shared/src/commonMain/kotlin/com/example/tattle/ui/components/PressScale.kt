package com.example.tattle.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/** Touch feedback like a native iPhone button: it dips slightly while pressed and springs back. Pass the same source to the component. */
@Composable
fun Modifier.pressScale(source: MutableInteractionSource, pressedScale: Float = 0.97f): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) pressedScale else 1f, spring(dampingRatio = 0.6f, stiffness = 700f), label = "pressScale")
    return this.graphicsLayer { scaleX = scale; scaleY = scale }
}

@Composable
fun rememberPressSource(): MutableInteractionSource = remember { MutableInteractionSource() }
