package com.example.tattle.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun AdVideoPlayer(url: String, modifier: Modifier) { Box(modifier) }

@Composable
actual fun PlayableAdView(url: String, modifier: Modifier, onCta: (String?) -> Unit) { Box(modifier) }
