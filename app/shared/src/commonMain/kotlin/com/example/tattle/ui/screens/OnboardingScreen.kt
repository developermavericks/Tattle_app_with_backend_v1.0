package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.ArticleRepository
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import org.koin.compose.koinInject

data class Interest(val id: String, val icon: String, val displayName: String)

@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    val articleRepository = koinInject<ArticleRepository>()
    var step by remember { mutableStateOf(1) }
    var language by remember { mutableStateOf("English") }
    var age by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    val selectedInterests = remember { mutableStateListOf<String>() }
    var notificationsEnabled by remember { mutableStateOf<Boolean?>(null) }

    val staticInterests = listOf(
        "🎬" to "entertainment",
        "🎞️" to "bollywood",
        "🎮" to "gaming",
        "💻" to "tech",
        "🌍" to "climate",
        "🎶" to "pop_culture",
        "💰" to "money",
        "🚀" to "science_space",
        "👗" to "fashion",
        "🤖" to "ai_robotics",
        "🧘" to "wellness",
        "⚽" to "sports"
    ).toMap()

    var dynamicInterests by remember { mutableStateOf<List<Interest>>(emptyList()) }

    LaunchedEffect(Unit) {
        val sectors = articleRepository.getSectors()
        val finalSectors = if (sectors.isNotEmpty()) {
            sectors
        } else {
            // Full list of 16 sectors as fallback
            listOf(
                "ai", "tech", "gaming", "money and business", "pop culture", 
                "sports", "science and space", "climate and environment", 
                "media and entertainment", "bollywood", "fashion", "wellness",
                "healthcare", "education", "geopolitics", "lifestyle"
            )
        }

        dynamicInterests = finalSectors.map { sector ->
            val icon = when {
                sector.contains("ai", true) -> "🤖"
                sector.contains("tech", true) -> "💻"
                sector.contains("gaming", true) -> "🎮"
                sector.contains("money", true) || sector.contains("business", true) -> "💰"
                sector.contains("pop culture", true) || sector.contains("pop_culture", true) -> "🎶"
                sector.contains("sport", true) -> "⚽"
                sector.contains("space", true) || sector.contains("science", true) -> "🚀"
                sector.contains("climate", true) || sector.contains("environment", true) -> "🌍"
                sector.contains("media", true) || sector.contains("entertainment", true) -> "🎬"
                sector.contains("bollywood", true) -> "🎞️"
                sector.contains("fashion", true) -> "👗"
                sector.contains("wellness", true) -> "🧘"
                sector.contains("health", true) -> "🏥"
                sector.contains("education", true) -> "📚"
                sector.contains("geopolitics", true) -> "🌐"
                sector.contains("lifestyle", true) -> "✨"
                sector.contains("creator", true) -> "🤳"
                else -> "📰"
            }
            // Capitalize for display
            val displayName = sector.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
            Interest(id = sector, icon = icon, displayName = displayName)
        }
    }

    Scaffold(
        topBar = {
            OnboardingHeader(step = step, language = language, onBack = { 
                if (step > 1) step-- else onBack() 
            })
        },
        containerColor = Color.White,
        bottomBar = {
            OnboardingFooter(
                step = step,
                language = language,
                canGoNext = when (step) {
                    1 -> language.isNotEmpty()
                    2 -> (age.isNotEmpty() && (age.toIntOrNull() ?: 0) in 13..100) || dob.length == 8
                    3 -> selectedInterests.size >= 3
                    else -> true
                },
                onNext = {
                    if (step < 4) {
                        step++
                    } else {
                        val prefs = UserPreferences(
                            isOnboarded = true,
                            ageGroup = if (age.isNotEmpty()) age else "18-24",
                            interests = selectedInterests.toList(),
                            language = language,
                            notifications = NotificationsPrefs(
                                dailyBriefings = notificationsEnabled ?: true,
                                breakingAlerts = notificationsEnabled ?: true,
                                streaks = notificationsEnabled ?: true
                            ),
                            streak = 1,
                            totalCardsRead = 0,
                            readHistory = emptyList(),
                            bookmarks = emptyList(),
                            reactions = emptyMap(),
                            adFreeUntil = null,
                            sensitivity = "Standard",
                            lastReadDate = null
                        )
                        onComplete(prefs)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }.using(SizeTransform(clip = false))
                },
                label = "OnboardingStepAnimation"
            ) { targetStep ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (targetStep) {
                        1 -> LanguageStep(
                            selectedLanguage = language,
                            onLanguageSelected = { language = it }
                        )
                        2 -> AgeStep(
                            age = age, 
                            dob = dob,
                            language = language, 
                            onAgeChange = { if (it.length <= 3) age = it.filter { c -> c.isDigit() } },
                            onDobChange = { if (it.length <= 8) dob = it.filter { c -> c.isDigit() } }
                        )
                        3 -> InterestStep(
                            interests = dynamicInterests,
                            selectedInterests = selectedInterests,
                            language = language,
                            onToggle = { id ->
                                if (selectedInterests.contains(id)) selectedInterests.remove(id)
                                else selectedInterests.add(id)
                            }
                        )
                        4 -> NotificationStep(
                            isEnabled = notificationsEnabled == true,
                            language = language,
                            onToggle = { notificationsEnabled = !(notificationsEnabled ?: false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingHeader(step: Int, language: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(24.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, LocalStrings.get("back", language), tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(step / 4f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(Primary)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
fun OnboardingFooter(
    step: Int,
    language: String,
    canGoNext: Boolean,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Button(
            onClick = onNext,
            enabled = canGoNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF232323),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFFE8E8E8),
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(
                text = if (step == 4) LocalStrings.get("finish", language) else LocalStrings.get("next", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun LanguageStep(selectedLanguage: String, onLanguageSelected: (String) -> Unit) {
    val languages = listOf("English", "Hindi")
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = LocalStrings.get("select_your_language", selectedLanguage),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = LocalStrings.get("choose_primary_language", selectedLanguage),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        languages.forEach { lang ->
            val isSelected = lang == selectedLanguage
            Surface(
                onClick = { onLanguageSelected(lang) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(64.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Primary else Color(0xFFF5F5F5),
                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = lang,
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AgeStep(age: String, dob: String, language: String, onAgeChange: (String) -> Unit, onDobChange: (String) -> Unit) {
    val isAgeError = age.isNotEmpty() && (age.toIntOrNull() ?: 0) !in 13..100
    
    // Proper DOB Validation
    val isDobError = remember(dob) {
        if (dob.length == 8) {
            try {
                val day = dob.substring(0, 2).toInt()
                val month = dob.substring(2, 4).toInt()
                val year = dob.substring(4, 8).toInt()
                
                // Basic check for future date (hardcoded current year as 2026 for prototype context)
                if (year > 2026) true
                else if (year == 2026 && month > 8) true // Mocking August 2026
                else month !in 1..12 || day !in 1..31
            } catch (e: Exception) {
                true
            }
        } else dob.isNotEmpty() && dob.length < 8
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = LocalStrings.get("your_age", language),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = LocalStrings.get("age_desc", language),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        // DOB Section
        Text(
            text = LocalStrings.get("dob_label", language),
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Box(modifier = Modifier.width(280.dp), contentAlignment = Alignment.Center) {
            TextField(
                value = dob,
                onValueChange = {
                    if (it.length <= 8) {
                        val filtered = it.filter { c -> c.isDigit() }
                        onDobChange(filtered)
                    }
                },
                textStyle = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = if (isDobError) Primary else Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (isDobError) Primary else Color.Black,
                    unfocusedIndicatorColor = Color(0xFFE8E8E8),
                    cursorColor = Primary
                ),
                placeholder = {
                    Text(
                        "DD/MM/YYYY",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE8E8E8),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(LocalStrings.get("type_age_hint", language), color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Age Section
        Text(
            text = LocalStrings.get("age_label", language),
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(modifier = Modifier.width(150.dp), contentAlignment = Alignment.Center) {
            TextField(
                value = age,
                onValueChange = onAgeChange,
                textStyle = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = if (isAgeError) Primary else Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (isAgeError) Primary else Color.Black,
                    unfocusedIndicatorColor = Color(0xFFE8E8E8),
                    cursorColor = Primary
                ),
                placeholder = {
                    Text(
                        "00",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE8E8E8),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }
        
        if (isAgeError || (dob.length == 8 && isDobError)) {
            Text(
                text = if (isAgeError) LocalStrings.get("invalid_age", language) else LocalStrings.get("invalid_date", language),
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): TransformedText {
        val input = text.text
        var out = ""
        for (i in input.indices) {
            out += input[i]
            if (i == 1 || i == 3) out += "/"
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 4) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }

        return TransformedText(androidx.compose.ui.text.AnnotatedString(out), offsetMapping)
    }
}

@Composable
fun ColumnScope.InterestStep(interests: List<Interest>, selectedInterests: SnapshotStateList<String>, language: String, onToggle: (String) -> Unit) {
    Text(
        text = LocalStrings.get("what_are_you_into", language),
        color = Color.Black,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
    Text(
        text = LocalStrings.get("pick_interests", language),
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().weight(1f)
    ) {
        items(interests) { interest ->
            val isSelected = selectedInterests.contains(interest.id)
            Card(
                onClick = { onToggle(interest.id) },
                modifier = Modifier.aspectRatio(0.85f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Primary else Color(0xFFF5F5F5)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = interest.icon,
                        fontSize = 32.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = interest.displayName,
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationStep(isEnabled: Boolean, language: String, onToggle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = LocalStrings.get("stay_in_loop", language),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = if (!isEnabled) LocalStrings.get("notification_desc", language) else LocalStrings.get("you_are_all_set", language),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp),
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(if (isEnabled) Primary else Color(0xFFF5F5F5))
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                contentDescription = "Notification Bell",
                modifier = Modifier.size(100.dp),
                tint = if (isEnabled) Color.White else Color.Gray
            )
        }
    }
}
