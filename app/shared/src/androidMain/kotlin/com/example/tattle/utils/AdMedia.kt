package com.example.tattle.utils

import android.annotation.SuppressLint
import android.net.Uri
import android.view.MotionEvent
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun AdVideoPlayer(url: String, modifier: Modifier) {
    val holder = remember(url) { arrayOfNulls<VideoView>(1) }
    DisposableEffect(url) { onDispose { try { holder[0]?.stopPlayback() } catch (_: Exception) { } } }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            VideoView(ctx).apply {
                holder[0] = this
                setVideoURI(Uri.parse(url))
                setOnPreparedListener { mp -> mp.isLooping = true; mp.setVolume(0f, 0f); start() }          // an ad starts muted and repeats
                setOnErrorListener { _, _, _ -> true }                                                         // a broken file leaves the black box, never a crash
            }
        }
    )
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
actual fun PlayableAdView(url: String, modifier: Modifier, onCta: (String?) -> Unit) {
    val latestCta by rememberUpdatedState(onCta)
    val pageHost = remember(url) { try { Uri.parse(url).host } catch (_: Exception) { null } }
    val holder = remember(url) { arrayOfNulls<WebView>(1) }
    DisposableEffect(url) { onDispose { try { holder[0]?.destroy() } catch (_: Exception) { } } }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                holder[0] = this
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                setBackgroundColor(0xFF14161C.toInt())
                isVerticalScrollBarEnabled = false
                // the game gets every touch: the scrolling card around it must not steal the swipes
                setOnTouchListener { v, e ->
                    if (e.actionMasked == MotionEvent.ACTION_DOWN) v.parent?.requestDisallowInterceptTouchEvent(true)
                    false
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val target = request.url
                        return when {
                            target.scheme == "tattle" -> { latestCta(null); true }
                            target.scheme in listOf("http", "https") && target.host == pageHost -> false             // the game's own pages
                            else -> { latestCta(target.toString()); true }                                      // a store link: out of the game
                        }
                    }
                }
                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(m: android.webkit.ConsoleMessage): Boolean { android.util.Log.d("TattleAd", m.message()); return true }
                }
                loadUrl(url)
            }
        }
    )
}
