package com.example.tattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Logo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glow Backing
        Box(
            modifier = Modifier
                .size(size * 0.9f)
                .blur(20.dp)
                .graphicsLayer(alpha = pulseAlpha)
                .drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFF5B3FF), Color(0xFFD176FF), Color(0xFF7582FF))
                        ),
                        alpha = 0.5f
                    )
                }
        )

        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = size.toPx()
            val scale = canvasWidth / 200f

            val tattleGrad = Brush.verticalGradient(
                colors = listOf(Color(0xFFF5B3FF), Color(0xFFD176FF), Color(0xFF7582FF))
            )
            val sparkleGrad = Brush.linearGradient(
                colors = listOf(Color.White, Color(0xFFF5B3FF))
            )

            // Speech bubble path
            val bubblePath = Path().apply {
                moveTo(80f * scale, 30f * scale)
                lineTo(120f * scale, 30f * scale)
                cubicTo(142f * scale, 30f * scale, 155f * scale, 42f * scale, 155f * scale, 65f * scale)
                lineTo(155f * scale, 105f * scale)
                cubicTo(155f * scale, 128f * scale, 142f * scale, 140f * scale, 120f * scale, 140f * scale)
                lineTo(85f * scale, 140f * scale)
                cubicTo(78f * scale, 140f * scale, 74f * scale, 152f * scale, 70f * scale, 168f * scale)
                cubicTo(68f * scale, 175f * scale, 64f * scale, 175f * scale, 66f * scale, 168f * scale)
                cubicTo(70f * scale, 152f * scale, 60f * scale, 140f * scale, 45f * scale, 140f * scale)
                cubicTo(45f * scale, 132f * scale, 45f * scale, 125f * scale, 45f * scale, 120f * scale)
                lineTo(45f * scale, 65f * scale)
                cubicTo(45f * scale, 42f * scale, 58f * scale, 30f * scale, 80f * scale, 30f * scale)
                close()
            }

            drawPath(
                path = bubblePath,
                color = Color.White.copy(alpha = 0.03f)
            )
            drawPath(
                path = bubblePath,
                brush = tattleGrad,
                style = Stroke(width = 9f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Stylized 'T'
            val tPath = Path().apply {
                moveTo(70f * scale, 60f * scale)
                lineTo(130f * scale, 60f * scale)
                lineTo(130f * scale, 74f * scale)
                lineTo(107f * scale, 74f * scale)
                lineTo(107f * scale, 118f * scale)
                cubicTo(107f * scale, 126f * scale, 93f * scale, 126f * scale, 93f * scale, 118f * scale)
                lineTo(93f * scale, 90f * scale)
                cubicTo(93f * scale, 78f * scale, 80f * scale, 74f * scale, 80f * scale, 74f * scale)
                lineTo(70f * scale, 74f * scale)
                close()
            }

            drawPath(
                path = tPath,
                color = Color.White
            )
            drawPath(
                path = tPath,
                brush = tattleGrad,
                style = Stroke(width = 6f * scale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Sparkles
            rotate(15f, pivot = Offset(144f * scale, 17f * scale)) {
                drawRoundRect(
                    brush = sparkleGrad,
                    topLeft = Offset(141f * scale, 10f * scale),
                    size = androidx.compose.ui.geometry.Size(6f * scale, 14f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale)
                )
            }

            // 4-point star
            val starPath = Path().apply {
                moveTo(163f * scale, 12f * scale)
                quadraticTo(163f * scale, 22f * scale, 173f * scale, 22f * scale)
                quadraticTo(163f * scale, 22f * scale, 163f * scale, 32f * scale)
                quadraticTo(163f * scale, 22f * scale, 153f * scale, 22f * scale)
                quadraticTo(163f * scale, 22f * scale, 163f * scale, 12f * scale)
                close()
            }
            drawPath(path = starPath, brush = sparkleGrad)

            rotate(-15f, pivot = Offset(173f * scale, 34f * scale)) {
                drawRoundRect(
                    brush = sparkleGrad,
                    topLeft = Offset(166f * scale, 31f * scale),
                    size = androidx.compose.ui.geometry.Size(14f * scale, 6f * scale),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * scale)
                )
            }
        }
    }
}
