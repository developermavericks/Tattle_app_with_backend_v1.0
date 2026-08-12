package com.example.tattle.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*

@Composable
fun RecapScreen(
    preferences: UserPreferences,
    onTuneFeed: () -> Unit,
) {
    val cardsRead = preferences.totalCardsRead.coerceAtLeast(15)
    val streakDays = preferences.streak.coerceAtLeast(12)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Status Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(Primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "LIMIT REACHED",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Daily Recap",
                color = Color.Black,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Come back later — new stories drop throughout the day.",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(300.dp),
                lineHeight = 22.sp
            )
        }

        // Metrics Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Book,
                value = cardsRead.toString(),
                label = "Cards Read",
                iconColor = Primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Whatshot,
                value = "$streakDays Days",
                label = "Streak",
                iconColor = Primary
            )
        }

        // Percentage Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8))
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    ChartBar(modifier = Modifier.weight(1f), percentage = 0.4f, label = "40%", color = Primary)
                    ChartBar(modifier = Modifier.weight(1f), percentage = 0.3f, label = "30%", color = Color.DarkGray)
                    ChartBar(modifier = Modifier.weight(1f), percentage = 0.2f, label = "20%", color = Color.Gray)
                    ChartBar(modifier = Modifier.weight(1f), percentage = 0.1f, label = "10%", color = Color.LightGray)
                }

                HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 1.dp)

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LegendItem(modifier = Modifier.weight(1f), color = Primary, label = "Tech & AI")
                        LegendItem(modifier = Modifier.weight(1f), color = Color.DarkGray, label = "Pop Culture")
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        LegendItem(modifier = Modifier.weight(1f), color = Color.Gray, label = "Politics")
                        LegendItem(modifier = Modifier.weight(1f), color = Color.LightGray, label = "Other Genres")
                    }
                }
            }
        }

        // Actions
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232323), contentColor = Color.White),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("SHARE YOUR RECAP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            TextButton(
                onClick = onTuneFeed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("TUNE YOUR FEED", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ChartBar(modifier: Modifier = Modifier, percentage: Float, label: String, color: Color) {
    val animatedHeight by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(1200)
    )

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(color.copy(alpha = 0.1f), RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedHeight)
                    .background(color, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LegendItem(modifier: Modifier = Modifier, color: Color, label: String) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Text(text = label, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
