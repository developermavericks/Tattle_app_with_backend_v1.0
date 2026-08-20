package com.example.tattle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val shimmerColor = Color.LightGray.copy(alpha = alpha)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFF5F5F5))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(shimmerColor)
            )

            // Info Placeholder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(top = 16.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
            ) {
                // Headline Lines
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp).background(shimmerColor, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(24.dp).background(shimmerColor, RoundedCornerShape(4.dp)))
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Body lines
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(shimmerColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth().height(14.dp).background(shimmerColor, RoundedCornerShape(2.dp)))
                Spacer(modifier = Modifier.height(6.dp))
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(shimmerColor, RoundedCornerShape(2.dp)))

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp).background(shimmerColor, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.width(80.dp).height(12.dp).background(shimmerColor, RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}
