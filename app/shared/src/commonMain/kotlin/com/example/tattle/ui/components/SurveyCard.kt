package com.example.tattle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.Survey
import com.example.tattle.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SurveyCard(
    survey: Survey,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var started by remember { mutableStateOf(value = false) }
    var currentQuestionIdx by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<String, String>() }
    var completed by remember { mutableStateOf(false) }

    val totalQuestions = survey.questions.size
    val currentQuestion = survey.questions.getOrNull(currentQuestionIdx)

    LaunchedEffect(completed) {
        if (completed) {
            delay(1200)
            onComplete()
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Decorative background circle
            Box(
                modifier = Modifier
                    .offset(x = 100.dp, y = (-100).dp)
                    .size(144.dp)
                    .blur(40.dp)
                    .background(Primary.copy(alpha = 0.05f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TATTLE CHECKPOINT",
                            color = Primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }

                    if (started && !completed) {
                        Text(
                            text = "Step ${currentQuestionIdx + 1} of $totalQuestions",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = when {
                            completed -> SurveyState.COMPLETED
                            started -> SurveyState.QUESTION
                            else -> SurveyState.INTRO
                        },
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "SurveyStateAnimation"
                    ) { state ->
                        when (state) {
                            SurveyState.INTRO -> {
                                IntroState { started = true }
                            }
                            SurveyState.QUESTION -> {
                                currentQuestion?.let { q ->
                                    QuestionState(
                                        question = q.question,
                                        options = q.options,
                                        onSelect = { opt ->
                                            selectedAnswers[q.id] = opt
                                            if (currentQuestionIdx < (totalQuestions - 1)) {
                                                currentQuestionIdx++
                                            } else {
                                                completed = true
                                            }
                                        }
                                    )
                                }
                            }
                            SurveyState.COMPLETED -> {
                                CompletedState()
                            }
                        }
                    }
                }

                // Footer for Intro
                if (!started) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 1.dp)
                        TextButton(
                            onClick = onSkip,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, null, modifier = Modifier.size(14.dp), tint = TextMuted)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "REMIND ME LATER",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        TextButton(
                            onClick = onWatchAd,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(14.dp), tint = TextMuted.copy(alpha = 0.6f))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "WATCH A STANDARD VIDEO AD INSTEAD",
                                    color = TextMuted.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private enum class SurveyState { INTRO, QUESTION, COMPLETED }

@Composable
private fun IntroState(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(112.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape)
                .border(1.dp, Primary.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Keep your feed 100% ad-free",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Provide quick feedback on three opinions to clear your next reading session.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.Black),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "START QUICK SURVEY", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun QuestionState(
    question: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = question,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 20.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        options.forEach { opt ->
            Card(
                onClick = { onSelect(opt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Background.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Text(
                    text = opt,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun CompletedState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Secondary.copy(alpha = 0.2f), CircleShape)
                .border(1.dp, Secondary.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Secondary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Checkpoint Cleared!",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "AD-FREE ACTIVE • 3 HOURS",
            color = Secondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your session is now unlocked and 100% ad-free. Keep stayin' sharp!",
            color = TextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
