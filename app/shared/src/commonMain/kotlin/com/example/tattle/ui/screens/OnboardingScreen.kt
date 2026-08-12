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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import tattle.app.shared.generated.resources.*

data class Interest(val id: String, val label: String, val icon: String)

@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var language by remember { mutableStateOf("English") }
    var age by remember { mutableStateOf("") }
    val selectedInterests = remember { mutableStateListOf<String>() }
    var notificationsEnabled by remember { mutableStateOf<Boolean?>(null) }

    val interestsList = listOf(
        Interest("Entertainment", "Entertainment", "🎬"),
        Interest("Bollywood", "Bollywood", "🎞️"),
        Interest("Gaming", "Gaming", "🎮"),
        Interest("Tech", "Tech", "💻"),
        Interest("Climate", "Climate", "🌍"),
        Interest("Pop Culture", "Pop Culture", "🎶"),
        Interest("Money", "Money", "💰"),
        Interest("Science & Space", "Science & Space", "🚀"),
        Interest("Fashion", "Fashion", "👗"),
        Interest("AI & Robotics", "AI & Robotics", "🤖"),
        Interest("Wellness", "Wellness", "🧘"),
        Interest("Sports", "Sports", "⚽")
    )

    Scaffold(
        topBar = {
            OnboardingHeader(step = step, onBack = { 
                if (step > 1) step-- else onBack() 
            })
        },
        containerColor = Color.White,
        bottomBar = {
            OnboardingFooter(
                step = step,
                language = language,
                canGoNext = when (step) {
                    1 -> true
                    2 -> age.isNotEmpty() && (age.toIntOrNull() ?: 0) in 13..100
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
                        1 -> LanguageStep(selectedLanguage = language, onLanguageSelected = { language = it })
                        2 -> AgeStep(age = age, onAgeChange = { if (it.length <= 3) age = it.filter { c -> c.isDigit() } })
                        3 -> InterestStep(
                            interests = interestsList,
                            selectedInterests = selectedInterests,
                            onToggle = { id ->
                                if (selectedInterests.contains(id)) selectedInterests.remove(id)
                                else selectedInterests.add(id)
                            }
                        )
                        4 -> NotificationStep(
                            isEnabled = notificationsEnabled == true,
                            onToggle = { notificationsEnabled = !(notificationsEnabled ?: false) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingHeader(step: Int, onBack: () -> Unit) {
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(Res.string.back), tint = Color.Black)
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8E8E8))
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
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(Res.string.select_your_language),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(Res.string.choose_primary_language),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        listOf("English", "Spanish", "French", "German").forEach { lang ->
            val isSelected = lang == selectedLanguage
            Card(
                onClick = { onLanguageSelected(lang) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) Primary else Color(0xFFE8E8E8)
                )
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = lang,
                        color = if (isSelected) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AgeStep(age: String, onAgeChange: (String) -> Unit) {
    val isError = age.isNotEmpty() && (age.toIntOrNull() ?: 0) !in 13..100

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.your_age),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(Res.string.age_desc),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
        )

        Box(modifier = Modifier.width(200.dp), contentAlignment = Alignment.Center) {
            BasicTextField(
                value = age,
                onValueChange = onAgeChange,
                textStyle = TextStyle(
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = if (isError) Primary else Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(Primary),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            if (age.isEmpty()) {
                                Text(
                                    "00",
                                    color = Color(0xFFE8E8E8),
                                    fontSize = 64.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            innerTextField()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Underline
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(4.dp)
                                .background(
                                    color = if (isError) Primary else if (age.isNotEmpty()) Color.Black else Color(0xFFE8E8E8),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            )
        }
        
        if (isError) {
            Text(
                stringResource(Res.string.invalid_age),
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
fun ColumnScope.InterestStep(interests: List<Interest>, selectedInterests: SnapshotStateList<String>, onToggle: (String) -> Unit) {
    Text(
        text = stringResource(Res.string.what_are_you_into),
        color = Color.Black,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
    Text(
        text = stringResource(Res.string.pick_interests),
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
                        text = interest.label,
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
fun NotificationStep(isEnabled: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.stay_in_loop),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(Res.string.notification_desc),
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
        )

        Image(
            painter = painterResource(if (isEnabled) Res.drawable.notification else Res.drawable.notification_off),
            contentDescription = "Notification Bell",
            modifier = Modifier
                .size(240.dp)
                .clickable { onToggle() },
            contentScale = ContentScale.Fit
        )
    }
}
