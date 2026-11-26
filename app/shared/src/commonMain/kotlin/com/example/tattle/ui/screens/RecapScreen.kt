package com.example.tattle.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.RecapDto
import com.example.tattle.data.UserSync
import com.example.tattle.models.UserPreferences
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import com.example.tattle.ui.theme.*
import tattle.app.shared.generated.resources.*

data class SectorStat(
    val slug: String,
    val icon: DrawableResource,
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
    val surveysAnswered = recap?.surveysAnswered ?: 0

    // The sector the reader engages with most today is called out in red; every other sector is a neutral grey.
    val topSectorColor = Primary
    val otherSectorColor = if (isDark) Color(0xFF4A4A4A) else Color(0xFF757575)

    // All 16 sectors, live from the server's per-sector read share for today (see GET /api/user/recap -> "sectors").
    val allSectorsData = remember(recap, isDark) {
        val share = recap?.sectors.orEmpty().associate { it.slug to it.percentage.toFloat() }
        val baseList = listOf(
            SectorStat("ai", Res.drawable.sector_ai, "AI & Robotics", (share["ai"] ?: 0f), otherSectorColor),
            SectorStat("tech", Res.drawable.sector_tech, "Tech", (share["tech"] ?: 0f), otherSectorColor),
            SectorStat("world news", Res.drawable.sector_world_news, "World News", (share["world news"] ?: 0f), otherSectorColor),
            SectorStat("money and business", Res.drawable.sector_business, "Business", (share["money and business"] ?: 0f), otherSectorColor),
            SectorStat("science and space", Res.drawable.sector_space, "Space", (share["science and space"] ?: 0f), otherSectorColor),
            SectorStat("climate and environment", Res.drawable.sector_climate, "Climate", (share["climate and environment"] ?: 0f), otherSectorColor),
            SectorStat("geopolitics", Res.drawable.sector_geopolitics, "Geopolitics", (share["geopolitics"] ?: 0f), otherSectorColor),
            SectorStat("healthcare", Res.drawable.sector_healthcare, "Healthcare", (share["healthcare"] ?: 0f), otherSectorColor),
            SectorStat("gaming", Res.drawable.sector_gaming, "Gaming", (share["gaming"] ?: 0f), otherSectorColor),
            SectorStat("sports", Res.drawable.sector_sports, "Sports", (share["sports"] ?: 0f), otherSectorColor),
            SectorStat("lifestyle", Res.drawable.sector_lifestyle, "Lifestyle", (share["lifestyle"] ?: 0f), otherSectorColor),
            SectorStat("startups", Res.drawable.sector_startups, "Startups", (share["startups"] ?: 0f), otherSectorColor),
            SectorStat("creator economy", Res.drawable.sector_creators, "Creators", (share["creator economy"] ?: 0f), otherSectorColor),
            SectorStat("education", Res.drawable.sector_education, "Education", (share["education"] ?: 0f), otherSectorColor),
            SectorStat("pop culture", Res.drawable.sector_pop_culture, "Pop Culture", (share["pop culture"] ?: 0f), otherSectorColor),
            SectorStat("media and entertainment", Res.drawable.sector_media, "Media", (share["media and entertainment"] ?: 0f), otherSectorColor)
        )

        val maxPercentage = baseList.maxOf { it.percentage }
        var leaderColored = false
        baseList
            .sortedByDescending { it.percentage }
            .map { sector ->
                if (maxPercentage > 0f && !leaderColored && sector.percentage == maxPercentage) {
                    leaderColored = true
                    sector.copy(color = topSectorColor)
                } else sector
            }
    }

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
                    borderColor = dividerColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Whatshot,
                    value = "$streakDays Days",
                    label = "Reading Streak",
                    iconColor = Primary,
                    borderColor = dividerColor,
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
                    icon = Icons.Default.Quiz,
                    value = surveysAnswered.toString(),
                    label = "Surveys Answered",
                    iconColor = Primary,
                    borderColor = dividerColor,
                    textColor = textColor,
                    subTextColor = subTextColor
                )
                MetricTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.EmojiEvents,
                    value = recap?.topPercent?.let { "Top $it%" } ?: "—",
                    label = "Reader Rank",
                    iconColor = Primary,
                    borderColor = dividerColor,
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

        // Sector Interest Distribution: a two-column list, each sector's own row carrying a short pill bar, live from today's reading.
        val leftColumn = allSectorsData.filterIndexed { index, _ -> index % 2 == 0 }
        val rightColumn = allSectorsData.filterIndexed { index, _ -> index % 2 == 1 }
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                leftColumn.forEach { sector -> SectorRow(sector = sector, trackColor = dividerColor) }
            }
            Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(dividerColor))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rightColumn.forEach { sector -> SectorRow(sector = sector, trackColor = dividerColor) }
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
    borderColor: Color,
    textColor: Color,
    subTextColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = value, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, color = subTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** One sector's row in the two-column Sector Interest Distribution list: icon, name, a short pill track and its percentage,
 * all sharing the same color - red for whichever sector led today's reading, grey for the rest. */
@Composable
fun SectorRow(
    sector: SectorStat,
    trackColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Image(
            painter = painterResource(sector.icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(sector.color),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = sector.name,
            color = sector.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 13.sp,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(trackColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(sector.percentage.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(sector.color)
            )
        }
        Text(
            text = "${(sector.percentage * 100).toInt()}%",
            color = sector.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.width(30.dp)
        )
    }
}
