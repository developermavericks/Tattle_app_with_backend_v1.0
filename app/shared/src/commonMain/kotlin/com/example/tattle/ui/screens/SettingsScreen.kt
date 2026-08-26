package com.example.tattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.MockData
import com.example.tattle.models.Article
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdatePreferences: (UserPreferences) -> Unit,
    onOpenArticle: (Article) -> Unit,
    onPurgeData: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showLanguageDialog by remember { mutableStateOf(false) }
    val language = LocalAppLanguage.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = LocalStrings.get("settings", language), color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Text(text = LocalStrings.get("configure_experience", language), color = Color.Gray, fontSize = 14.sp)
            }
        }

        // Notifications
        SettingsSection(title = LocalStrings.get("notifications", language)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SwitchItem(
                        title = LocalStrings.get("daily_briefings", language),
                        subtitle = LocalStrings.get("daily_briefings_desc", language),
                        checked = preferences.notifications.dailyBriefings,
                        onCheckedChange = { 
                            onUpdatePreferences(preferences.copy(notifications = preferences.notifications.copy(dailyBriefings = it))) 
                        }
                    )
                    SwitchItem(
                        title = LocalStrings.get("breaking_alerts", language),
                        subtitle = LocalStrings.get("breaking_alerts_desc", language),
                        checked = preferences.notifications.breakingAlerts,
                        onCheckedChange = { 
                            onUpdatePreferences(preferences.copy(notifications = preferences.notifications.copy(breakingAlerts = it))) 
                        }
                    )
                }
            }
        }

        // Interests Tuning
        SettingsSection(title = LocalStrings.get("your_interests", language)) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                preferences.interests.forEach { interest ->
                    AssistChip(
                        onClick = {
                            onUpdatePreferences(preferences.copy(interests = preferences.interests - interest))
                        },
                        label = { Text(interest, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Primary,
                            labelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                
                AssistChip(
                    onClick = { /* Add more logic */ },
                    label = { Text(LocalStrings.get("add", language), fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFF5F5F5),
                        labelColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Preferences
        SettingsSection(title = LocalStrings.get("preferences", language)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    MenuItem(
                        icon = Icons.Default.Language,
                        title = LocalStrings.get("language", language),
                        subtitle = preferences.language,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    MenuItem(
                        icon = Icons.Default.Delete,
                        title = LocalStrings.get("clear_history", language),
                        subtitle = LocalStrings.get("clear_history_desc", language),
                        onClick = onPurgeData
                    )
                }
            }
        }

        // Read History
        val historyArticles = MockData.articles.filter { preferences.readHistory.contains(it.id) }
        if (historyArticles.isNotEmpty()) {
            SettingsSection(title = LocalStrings.get("read_history_label", language)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        historyArticles.reversed().take(5).forEachIndexed { index, article ->
                            MenuItem(
                                icon = Icons.Default.History,
                                title = article.headline,
                                subtitle = "Read on ${article.publishedAt}", // Simplification: using publishedAt as a proxy or just showing it's read
                                onClick = { onOpenArticle(article) }
                            )
                            if (index < historyArticles.take(5).size - 1) {
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Saved Articles
        val savedArticles = MockData.articles.filter { preferences.bookmarks.contains(it.id) }
        if (savedArticles.isNotEmpty()) {
            SettingsSection(title = LocalStrings.get("saved_articles_label", language)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(modifier = Modifier.padding(vertical = 10.dp)) {
                        savedArticles.reversed().take(5).forEachIndexed { index, article ->
                            MenuItem(
                                icon = Icons.Default.Bookmark,
                                title = article.headline,
                                subtitle = article.category,
                                onClick = { onOpenArticle(article) }
                            )
                            if (index < savedArticles.take(5).size - 1) {
                                HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                            }
                        }
                    }
                }
            }
        }

        Text(
            text = "TATTLE v3.0.0",
            color = Color.LightGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp),
            textAlign = TextAlign.Center
        )
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = Color.White,
            title = { Text(LocalStrings.get("select_language", language), color = Color.Black) },
            text = {
                Column {
                    listOf("English", "Hindi").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdatePreferences(preferences.copy(language = lang))
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (preferences.language == lang),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = Primary)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = lang, color = Color.Black)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(LocalStrings.get("close", language), color = Primary)
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        content()
    }
}

@Composable
fun SwitchItem(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun MenuItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(icon, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            Column {
                Text(text = title, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.LightGray, modifier = Modifier.size(20.dp))
    }
}
