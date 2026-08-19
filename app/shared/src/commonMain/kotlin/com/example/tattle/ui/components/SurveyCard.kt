package com.example.tattle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
    onWatchAd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language = LocalAppLanguage.current
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isFinished by remember { mutableStateOf(false) }

    val currentQuestion = survey.questions[currentQuestionIndex]
    val progress = (currentQuestionIndex + 1).toFloat() / survey.questions.size

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(550.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Progress
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color(0xFFF5F5F5))
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
                                text = LocalStrings.get("help_improve", language),
                                color = Primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${currentQuestionIndex + 1}/${survey.questions.size}",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Text(
                            text = LocalStrings.get("skip", language),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onSkip() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = currentQuestion.question,
                        color = Color.Black,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        currentQuestion.options.forEachIndexed { index, option ->
                            val isSelected = selectedOptionIndex == index
                            Card(
                                onClick = { selectedOptionIndex = index },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Primary else Color(0xFFF5F5F5)
                                ),
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color.White)
                                            .border(1.dp, if (isSelected) Color.White else Color.Black.copy(alpha = 0.1f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = option,
                                        color = if (isSelected) Color.White else Color.Black,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
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
                            .height(64.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF232323),
                            disabledContainerColor = Color(0xFFE8E8E8)
                        )
                    ) {
                        Text(
                            text = if (currentQuestionIndex == survey.questions.size - 1) LocalStrings.get("finish", language) else LocalStrings.get("next", language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Celebration, null, tint = Primary, modifier = Modifier.size(40.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = LocalStrings.get("claim_ad_free", language),
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = LocalStrings.get("ad_desc", language),
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = onWatchAd,
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = RoundedCornerShape(32.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(LocalStrings.get("watch_ad", language), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onComplete) {
                            Text(LocalStrings.get("get_ad_free", language), color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
