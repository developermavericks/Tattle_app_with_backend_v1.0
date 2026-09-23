package com.example.tattle.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.models.Survey
import com.example.tattle.ui.theme.*

@Composable
fun SurveyCard(
    survey: Survey,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    isDark: Boolean = false,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQuestion = survey.questions.getOrNull(currentQuestionIndex) ?: survey.questions.first()
    val progress = (currentQuestionIndex + 1).toFloat() / survey.questions.size

    val bgColor = if (isDark) DarkSurface else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val optionCardBg = if (isDark) Color(0xFF282828) else Color(0xFFF5F5F5)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(560.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Progress Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .background(Primary)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                if (!isFinished) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "READER SURVEY 📋",
                                color = Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${survey.questions.size}",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = LocalStrings.get("skip", language),
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSkip() }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = currentQuestion.question,
                        color = textColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        currentQuestion.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptionIndex == index
                            Card(
                                onClick = { selectedOptionIndex = index },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Primary else optionCardBg
                                ),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White.copy(alpha = 0.25f) else Color.White)
                                            .border(1.dp, if (isSelected) Color.White else Color.Gray.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color.White else textColor,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (currentQuestionIndex < survey.questions.size - 1) {
                                currentQuestionIndex++
                                selectedOptionIndex = null
                            } else {
                                isFinished = true
                            }
                        },
                        enabled = selectedOptionIndex != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary,
                            disabledContainerColor = Color(0xFFCCCCCC)
                        )
                    ) {
                        Text(
                            text = if (currentQuestionIndex == survey.questions.size - 1) "Finish & Claim 1-Hour Pass ⚡" else "Next Question",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                } else {
                    // Reward / Finished State
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Celebration, null, tint = Primary, modifier = Modifier.size(38.dp))
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Survey Completed! 🎉",
                            color = textColor,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Thank you for your feedback! You have unlocked 1 hour of unlimited reading without ads or surveys.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Start 1-Hour Reading Pass ⚡", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
