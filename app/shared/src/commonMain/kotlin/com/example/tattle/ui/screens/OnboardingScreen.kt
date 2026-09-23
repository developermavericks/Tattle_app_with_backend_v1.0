package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.LoginRepository
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

data class DetailedAge(val years: Int, val months: Int, val days: Int) {
    fun toFormattedString(): String {
        val yStr = if (years == 1) "1 year" else "$years years"
        val mStr = if (months == 1) "1 month" else "$months months"
        val dStr = if (days == 1) "1 day" else "$days days"
        return "$yStr, $mStr, $dStr old"
    }
}

fun calculateDetailedAgeFromDob(dobString: String): DetailedAge? {
    if (dobString.length != 8) return null
    return try {
        val day = dobString.substring(0, 2).toInt()
        val month = dobString.substring(2, 4).toInt()
        val year = dobString.substring(4, 8).toInt()
        
        // Retrieve current time in local timezone
        val now = kotlinx.datetime.Instant.fromEpochMilliseconds(
            com.example.tattle.utils.currentTimeMillis()
        ).toLocalDateTime(TimeZone.currentSystemDefault())

        // 12:01 AM Midnight Rollover Cutoff:
        // Before 12:01 AM (hour 0, minute 0), age is evaluated as of yesterday.
        // From 12:01 AM onwards, age is evaluated as of the current day.
        val effectiveDate = if (now.hour == 0 && now.minute == 0) {
            kotlinx.datetime.Instant.fromEpochMilliseconds(
                com.example.tattle.utils.currentTimeMillis() - 24 * 3600 * 1000L
            ).toLocalDateTime(TimeZone.currentSystemDefault())
        } else {
            now
        }

        val currentYear = effectiveDate.year
        val currentMonth = effectiveDate.monthNumber
        val currentDay = effectiveDate.dayOfMonth

        if (month !in 1..12 || day !in 1..31 || year > currentYear || year < 1900) return null

        var y = currentYear - year
        var m = currentMonth - month
        var d = currentDay - day

        if (d < 0) {
            m -= 1
            val prevMonth = if (currentMonth == 1) 12 else currentMonth - 1
            val prevMonthYear = if (currentMonth == 1) currentYear - 1 else currentYear
            val isLeapYear = (prevMonthYear % 4 == 0 && prevMonthYear % 100 != 0) || (prevMonthYear % 400 == 0)
            val prevMonthDays = when (prevMonth) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear) 29 else 28
                else -> 30
            }
            d += prevMonthDays
        }

        if (m < 0) {
            y -= 1
            m += 12
        }

        if (y in 0..120 && !(y == 0 && m == 0 && d < 0)) DetailedAge(y, m, d) else null
    } catch (_: Exception) {
        null
    }
}

fun calculateAgeFromDob(dobString: String): Int? {
    return calculateDetailedAgeFromDob(dobString)?.years
}

@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val loginRepository = koinInject<LoginRepository>()
    var step by remember { mutableStateOf(1) }
    var dob by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf<Boolean?>(null) }

    val calculatedAge = calculateAgeFromDob(dob)

    val defaultSectors = listOf(
        "ai", "climate and environment", "creator economy", "education",
        "gaming", "geopolitics", "healthcare", "lifestyle",
        "media and entertainment", "money and business", "pop culture",
        "science and space", "sports", "startups", "tech", "world news"
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
                canGoNext = when (step) {
                    1 -> dob.length == 8 && calculatedAge != null
                    else -> true
                },
                onNext = {
                    if (step < 2) {
                        step++
                    } else {
                        val prefs = UserPreferences(
                            isOnboarded = true,
                            ageGroup = calculatedAge?.toString() ?: "18-24",
                            interests = defaultSectors,
                            language = "English",
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
                            lastReadDate = null,
                            dob = dob
                        )
                        scope.launch {
                            loginRepository.syncProfileWithServer(
                                dob = dob,
                                age = calculatedAge,
                                notificationsEnabled = notificationsEnabled ?: true
                            )
                        }
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
                        1 -> DobAgeStep(
                            dob = dob,
                            calculatedAge = calculatedAge,
                            onDobChange = { if (it.length <= 8) dob = it.filter { c -> c.isDigit() } }
                        )
                        2 -> NotificationStep(
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
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
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
                        .fillMaxWidth(step / 2f)
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
                text = if (step == 2) "Finish" else "Next",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun DobAgeStep(dob: String, calculatedAge: Int?, onDobChange: (String) -> Unit) {
    val isDobInvalid = dob.length == 8 && calculatedAge == null

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter Your Date of Birth",
            color = Color.Black,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "We use your date of birth to tailor news relevant to your demographic.",
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        // DOB Input Section
        Text(
            text = "DATE OF BIRTH",
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.width(280.dp), contentAlignment = Alignment.Center) {
            TextField(
                value = dob,
                onValueChange = {
                    if (it.length <= 8) {
                        onDobChange(it.filter { c -> c.isDigit() })
                    }
                },
                textStyle = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = if (isDobInvalid) Primary else Color.Black
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = if (isDobInvalid) Primary else Color.Black,
                    unfocusedIndicatorColor = Color(0xFFE8E8E8),
                    cursorColor = Primary
                ),
                placeholder = {
                    Text(
                        "DD/MM/YYYY",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        color = Color(0xFFCCCCCC),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Age Calculated Output Area
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFFF5F5F5),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE8E8E8))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "YOUR CALCULATED AGE",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val detailedAge = calculateDetailedAgeFromDob(dob)
                if (detailedAge != null) {
                    Text(
                        text = detailedAge.toFormattedString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary,
                        textAlign = TextAlign.Center
                    )
                } else if (isDobInvalid) {
                    Text(
                        text = "Invalid Date of Birth",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                } else {
                    Text(
                        text = "Enter your date if birth above",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
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
fun NotificationStep(isEnabled: Boolean, onToggle: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Stay in the Loop",
            color = Color.Black,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = if (!isEnabled) "Get instant breaking news alerts and daily briefings directly to your device." else "You are all set! Notifications enabled.",
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
