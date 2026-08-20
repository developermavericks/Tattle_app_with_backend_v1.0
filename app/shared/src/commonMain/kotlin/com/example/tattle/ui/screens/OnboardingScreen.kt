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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*

data class Interest(val id: String, val icon: String)

@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var language by remember { mutableStateOf("English") }
    var age by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    val selectedInterests = remember { mutableStateListOf<String>() }
    var notificationsEnabled by remember { mutableStateOf<Boolean?>(null) }

    val interestsList = listOf(
        Interest("entertainment", "🎬"),
        Interest("bollywood", "🎞️"),
        Interest("gaming", "🎮"),
        Interest("tech", "💻"),
        Interest("climate", "🌍"),
        Interest("pop_culture", "🎶"),
        Interest("money", "💰"),
        Interest("science_space", "🚀"),
        Interest("fashion", "👗"),
        Interest("ai_robotics", "🤖"),
        Interest("wellness", "🧘"),
        Interest("sports", "⚽")
    )

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
                    1 -> (age.isNotEmpty() && (age.toIntOrNull() ?: 0) in 13..100) || dob.length == 8
                    2 -> selectedInterests.size >= 3
                    else -> true
                },
                onNext = {
                    if (step < 3) {
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
                        1 -> AgeStep(
                            age = age, 
                            dob = dob,
                            language = language, 
                            onAgeChange = { if (it.length <= 3) age = it.filter { c -> c.isDigit() } },
                            onDobChange = { if (it.length <= 8) dob = it.filter { c -> c.isDigit() } }
                        )
                        2 -> InterestStep(
                            interests = interestsList,
                            selectedInterests = selectedInterests,
                            language = language,
                            onToggle = { id ->
                                if (selectedInterests.contains(id)) selectedInterests.remove(id)
                                else selectedInterests.add(id)
                            }
                        )
                        3 -> NotificationStep(
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
                        .fillMaxWidth(step / 3f)
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
                text = if (step == 3) LocalStrings.get("finish", language) else LocalStrings.get("next", language),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun AgeStep(age: String, dob: String, language: String, onAgeChange: (String) -> Unit, onDobChange: (String) -> Unit) {
    val isAgeError = age.isNotEmpty() && (age.toIntOrNull() ?: 0) !in 13..100

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "When were you born?",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = "So we can keep your feed age-appropriate.",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = dob,
            onValueChange = onDobChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("DDMMYYYY") },
            placeholder = { Text("e.g. 15081995") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text("or simply type your age", color = Color.Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

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
        
        if (isAgeError) {
            Text(
                LocalStrings.get("invalid_age", language),
                color = Primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
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
                        text = LocalStrings.get(interest.id, language),
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
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = LocalStrings.get("stay_in_loop", language),
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Text(
            text = if (!isEnabled) LocalStrings.get("notification_desc", language) else "You're all set! We'll keep you posted.",
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 48.dp)
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
