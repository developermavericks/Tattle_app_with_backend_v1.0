package com.example.tattle.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A video ad: plays muted and looping inside its box. */
@Composable
expect fun AdVideoPlayer(url: String, modifier: Modifier = Modifier)

/** A playable (mini-game) ad page. `onCta(url)` is called when the page asks for the store button: url is null for the built-in `tattle://cta`, otherwise the address it tried to open. */
@Composable
expect fun PlayableAdView(url: String, modifier: Modifier = Modifier, onCta: (String?) -> Unit)
