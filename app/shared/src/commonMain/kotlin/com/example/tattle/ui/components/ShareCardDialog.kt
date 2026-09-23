package com.example.tattle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.tattle.data.ImageRepository
import com.example.tattle.models.Article
import com.example.tattle.ui.theme.Primary
import com.example.tattle.utils.shareArticleContent
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import tattle.app.shared.generated.resources.*

@Composable
fun ShareCardDialog(
    article: Article,
    onDismiss: () -> Unit,
    onConfirmShare: (String) -> Unit = {}
) {
    val imageRepository = koinInject<ImageRepository>()
    var imageBitmap by remember(article.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(article.id) {
        imageBitmap = imageRepository.getOrFetchBitmapForArticle(article)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Bar (Tattle Logo & Close)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(Res.drawable.logo),
                            contentDescription = "Tattle Logo",
                            modifier = Modifier.height(26.dp),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(Color.Black)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFFF3F4F6), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Article Image Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
                ) {
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap!!,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary, modifier = Modifier.size(24.dp))
                        }
                    }
                }

                // Article Headline / Title
                Text(
                    text = article.headline,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827),
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                // Article Summary / Hook
                Text(
                    text = article.hook.ifBlank { article.brief },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563),
                    lineHeight = 17.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                // Publisher / Time Info
                Text(
                    text = "${article.publisher} • ${article.publishedAt}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B21A8),
                    modifier = Modifier.fillMaxWidth()
                )

                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)

                // Share Platform Icons Header
                Text(
                    text = "Share story via",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    modifier = Modifier.fillMaxWidth()
                )

                // Direct Platform Sharing Grid (WhatsApp, Telegram, Discord, Facebook, X, More)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShareChip(
                        name = "WhatsApp",
                        brandType = "whatsapp",
                        color = Color(0xFF25D366),
                        onClick = {
                            shareArticleContent(article, "whatsapp")
                            onConfirmShare("whatsapp")
                            onDismiss()
                        }
                    )
                    ShareChip(
                        name = "Telegram",
                        brandType = "telegram",
                        color = Color(0xFF0088CC),
                        onClick = {
                            shareArticleContent(article, "telegram")
                            onConfirmShare("telegram")
                            onDismiss()
                        }
                    )
                    ShareChip(
                        name = "Discord",
                        brandType = "discord",
                        color = Color(0xFF5865F2),
                        onClick = {
                            shareArticleContent(article, "discord")
                            onConfirmShare("discord")
                            onDismiss()
                        }
                    )
                    ShareChip(
                        name = "Facebook",
                        brandType = "facebook",
                        color = Color(0xFF1877F2),
                        onClick = {
                            shareArticleContent(article, "facebook")
                            onConfirmShare("facebook")
                            onDismiss()
                        }
                    )
                    ShareChip(
                        name = "X",
                        brandType = "twitter",
                        color = Color(0xFF14171A),
                        onClick = {
                            shareArticleContent(article, "twitter")
                            onConfirmShare("twitter")
                            onDismiss()
                        }
                    )
                    ShareChip(
                        name = "More",
                        brandType = "generic",
                        color = Primary,
                        onClick = {
                            shareArticleContent(article, "generic")
                            onConfirmShare("generic")
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ShareChip(
    name: String,
    brandType: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.8f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            when (brandType) {
                "whatsapp" -> Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(20.dp))
                "telegram" -> Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Telegram", tint = Color.White, modifier = Modifier.size(18.dp))
                "discord" -> Icon(Icons.Default.SportsEsports, contentDescription = "Discord", tint = Color.White, modifier = Modifier.size(20.dp))
                "facebook" -> Icon(Icons.Default.ThumbUp, contentDescription = "Facebook", tint = Color.White, modifier = Modifier.size(18.dp))
                "twitter" -> Icon(Icons.Default.Close, contentDescription = "X", tint = Color.White, modifier = Modifier.size(18.dp))
                else -> Icon(Icons.Default.Share, contentDescription = "More", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        Text(
            text = name,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4B5563)
        )
    }
}
