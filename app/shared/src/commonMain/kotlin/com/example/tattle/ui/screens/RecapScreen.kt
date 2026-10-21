package com.example.tattle.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.RecapDto
import com.example.tattle.data.UserSync
import com.example.tattle.models.UserPreferences
import org.koin.compose.koinInject
import com.example.tattle.ui.theme.*

data class SectorStat(
    val slug: String,
    val icon: String,
    val name: String,
    val percentage: Float, // 0.0 to 1.0
    val color: Color
)

@Composable
fun RecapScreen(
    preferences: UserPreferences,
    onTuneFeed: () -> Unit = {},
    onBack: () -> Unit
) {
    val isDark = preferences.isDarkMode

    val bgColor = if (isDark) DarkBackground else Color.White
    val cardBgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8F9FA)
    val textColor = if (isDark) Color.White else Color(0xFF111111)
    val subTextColor = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)
    val dividerColor = if (isDark) Color(0xFF2C2C2C) else Color(0xFFE5E7EB)

    // Real numbers from the server, computed from this user's own activity today (see GET /api/user/recap); zero until they read something.
    val userSync = koinInject<UserSync>()
    var recap by remember { mutableStateOf<RecapDto?>(null) }
    var recapLoaded by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        recap = userSync.fetchRecap()
        recapLoaded = true
    }
    val cardsRead = recap?.cardsRead ?: 0
    val streakDays = recap?.streakDays ?: 0
    val estimatedMinutes = recap?.minutes ?: 0

    // Palette for 16 sectors
    val sectorColors = listOf(
        Primary, Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
        Color(0xFF9C27B0), Color(0xFF00BCD4), Color(0xFFE91E63), Color(0xFF3F51B5),
        Color(0xFF009688), Color(0xFFFF5722), Color(0xFF8BC34A), Color(0xFFFFC107),
        Color(0xFF673AB7), Color(0xFF795548), Color(0xFF607D8B), Color(0xFF00E676)
    )

    // All 16 Sectors data dynamically mapped
    val allSectorsData = remember(recap) {
        val share = recap?.sectors.orEmpty().associate { it.slug to it.percentage.toFloat() }
        val baseList = listOf(
            SectorStat("ai", "🤖", "AI & Robotics", (share["ai"] ?: 0f), sectorColors[0]),
            SectorStat("tech", "💻", "Tech", (share["tech"] ?: 0f), sectorColors[1]),
            SectorStat("world news", "📰", "World News", (share["world news"] ?: 0f), sectorColors[2]),
            SectorStat("money and business", "💰", "Business", (share["money and business"] ?: 0f), sectorColors[3]),
            SectorStat("science and space", "🪐", "Space", (share["science and space"] ?: 0f), sectorColors[4]),
            SectorStat("climate and environment", "🌍", "Climate", (share["climate and environment"] ?: 0f), sectorColors[5]),
            SectorStat("geopolitics", "🌐", "Geopolitics", (share["geopolitics"] ?: 0f), sectorColors[6]),
            SectorStat("healthcare", "🏥", "Healthcare", (share["healthcare"] ?: 0f), sectorColors[7]),
            SectorStat("gaming", "🎮", "Gaming", (share["gaming"] ?: 0f), sectorColors[8]),
            SectorStat("sports", "⚽", "Sports", (share["sports"] ?: 0f), sectorColors[9]),
            SectorStat("lifestyle", "✨", "Lifestyle", (share["lifestyle"] ?: 0f), sectorColors[10]),
            SectorStat("startups", "🚀", "Startups", (share["startups"] ?: 0f), sectorColors[11]),
            SectorStat("creator economy", "🤳", "Creators", (share["creator economy"] ?: 0f), sectorColors[12]),
            SectorStat("education", "📚", "Education", (share["education"] ?: 0f), sectorColors[13]),
            SectorStat("pop culture", "🎶", "Pop Culture", (share["pop culture"] ?: 0f), sectorColors[14]),
            SectorStat("media and entertainment", "🎬", "Media", (share["media and entertainment"] ?: 0f), sectorColors[15])
        )

        baseList.sortedByDescending { it.percentage }
    }

    val hasReads = cardsRead > 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Back Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(cardBgColor, RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = textColor
                )
            }
        }

        // Status Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = Primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "TODAY'S RECAP ⚡",
                    color = Primary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "Your Daily Summary",
                color = textColor,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (recapLoaded && recap == null) "Can't reach the server right now. Connect to the same Wi-Fi as the Tattle PC to see your numbers."
                       else "Your reading today, updated live as you read.",
                color = subTextColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        // 4-Card Key Performance Metrics Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    value = cardsRead.toString(),
                    label = "Cards Read",
                    iconColor = Primary,
                    cardBgColor = cardBgColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Whatshot,
                    value = "$streakDays Days",
                    label = "Reading Streak",
                    iconColor = Color(0xFFFF9800),
                    cardBgColor = cardBgColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Timer,
                    value = "$estimatedMinutes Mins",
                    label = "Time Saved",
                    iconColor = Color(0xFF2196F3),
                    cardBgColor = cardBgColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.EmojiEvents,
                    value = recap?.topPercent?.let { "Top $it%" } ?: "—",
                    label = "Reader Rank",
                    iconColor = Color(0xFF4CAF50),
                    cardBgColor = cardBgColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
            }
        }

        // AI Insight Highlight Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI Reader Insights",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Primary
                    )
                    Text(
                        text = recap?.summary?.takeIf { it.isNotBlank() } ?: "Read a few stories and your recap will appear here.",
                        fontSize = 12.sp,
                        color = textColor,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 16-Sector Thin Bar Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = cardBgColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sector Interest Distribution (All 16)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = if (hasReads) "100% Total" else "No reads yet today",
                        fontSize = 12.sp,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 16 Thin Bars Chart
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    allSectorsData.forEach { sector ->
                        ThinChartBar(
                            modifier = Modifier.weight(1f),
                            percentage = sector.percentage,
                            icon = sector.icon,
                            color = sector.color
                        )
                    }
                }

                HorizontalDivider(color = dividerColor, thickness = 1.dp)

                // 16-Sector Legend Grid (4 Columns)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val chunks = allSectorsData.chunked(2)
                    chunks.forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { sector ->
                                SectorLegendTile(
                                    modifier = Modifier.weight(1f),
                                    sector = sector,
                                    textColor = textColor
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Bottom Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Continue Reading Feed ⚡", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MetricTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    cardBgColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = subTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ThinChartBar(
    modifier: Modifier = Modifier,
    percentage: Float,
    icon: String,
    color: Color
) {
    val animatedHeight by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(1000)
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.08f))
                    .background(color, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = icon, fontSize = 10.sp)
    }
}

@Composable
fun SectorLegendTile(
    modifier: Modifier = Modifier,
    sector: SectorStat,
    textColor: Color
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(sector.color)
        )
        Text(
            text = "${sector.icon} ${sector.name}",
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            text = "${(sector.percentage * 100).toInt()}%",
            color = sector.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
