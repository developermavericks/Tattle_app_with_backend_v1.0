package com.example.tattle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import tattle.app.shared.generated.resources.*

@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdatePreferences: (UserPreferences) -> Unit,
    onPurgeData: () -> Unit
) {
    val scrollState = rememberScrollState()
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = stringResource(Res.string.settings), color = Color.Black, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            Text(text = stringResource(Res.string.configure_experience), color = Color.Gray, fontSize = 16.sp)
        }

        // Notifications
        SettingsSection(title = stringResource(Res.string.notifications)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    SwitchItem(
                        title = stringResource(Res.string.daily_briefings),
                        subtitle = stringResource(Res.string.daily_briefings_desc),
                        checked = preferences.notifications.dailyBriefings,
                        onCheckedChange = { 
                            onUpdatePreferences(preferences.copy(notifications = preferences.notifications.copy(dailyBriefings = it))) 
                        }
                    )
                    SwitchItem(
                        title = stringResource(Res.string.breaking_alerts),
                        subtitle = stringResource(Res.string.breaking_alerts_desc),
                        checked = preferences.notifications.breakingAlerts,
                        onCheckedChange = { 
                            onUpdatePreferences(preferences.copy(notifications = preferences.notifications.copy(breakingAlerts = it))) 
                        }
                    )
                }
            }
        }

        // Interests Tuning
        SettingsSection(title = stringResource(Res.string.your_interests)) {
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
                    label = { Text(stringResource(Res.string.add), fontWeight = FontWeight.Bold) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFE8E8E8),
                        labelColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Preferences
        SettingsSection(title = stringResource(Res.string.preferences)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8E8E8))
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    MenuItem(
                        icon = Icons.Default.Language,
                        title = stringResource(Res.string.language),
                        subtitle = preferences.language,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.05f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                    MenuItem(
                        icon = Icons.Default.Delete,
                        title = stringResource(Res.string.clear_history),
                        subtitle = stringResource(Res.string.clear_history_desc),
                        onClick = onPurgeData
                    )
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
            title = { Text(stringResource(Res.string.select_language), color = Color.Black) },
            text = {
                Column {
                    listOf("English", "Spanish", "French", "German").forEach { lang ->
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
                    Text(stringResource(Res.string.close), color = Primary)
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

@Composable
fun AnalyticsItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
