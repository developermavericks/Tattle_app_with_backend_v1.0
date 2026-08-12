package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.ui.theme.Primary
import com.example.tattle.ui.theme.TextPrimary
import org.jetbrains.compose.resources.painterResource
import tattle.app.shared.generated.resources.Res
import tattle.app.shared.generated.resources.logo
import tattle.app.shared.generated.resources.world_map

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var showGoogleLogin by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Background Map
        Image(
            painter = painterResource(Res.drawable.world_map),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 20.dp),
            contentScale = ContentScale.FillWidth,
            alpha = 0.1f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Center Logo
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(1000)) + scaleIn(tween(1000), initialScale = 0.8f)
            ) {
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = "Tattle Logo",
                    modifier = Modifier.size(280.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // CTA Area
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(tween(800), initialOffsetY = { it / 2 }) + fadeIn(tween(800))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoginButton(
                        text = "Continue with Google",
                        icon = {
                            Text("G", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(end = 8.dp))
                        },
                        onClick = { showGoogleLogin = true }
                    )

                    LoginButton(
                        text = "Continue with Phone Number",
                        icon = {
                            Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
                        },
                        onClick = onGetStarted
                    )

                    LoginButton(
                        text = "Continue with Email",
                        icon = {
                            Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
                        },
                        onClick = onGetStarted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "By continuing, you agree to our Terms of Service",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }

        if (showGoogleLogin) {
            GoogleLoginOverlay(
                onDismiss = { showGoogleLogin = false },
                onLoginSuccess = {
                    showGoogleLogin = false
                    onGetStarted()
                }
            )
        }
    }
}

@Composable
fun GoogleLoginOverlay(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        title = { Text("Sign in with Google", color = Color.Black) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFFE14344))
                    Text("Authenticating...", modifier = Modifier.padding(top = 16.dp), color = Color.Gray)
                } else {
                    listOf("tattle.user@gmail.com", "another.account@gmail.com").forEach { email ->
                        Card(
                            onClick = {
                                isLoading = true
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(32.dp).background(Color.LightGray, androidx.compose.foundation.shape.CircleShape))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = email, color = Color.Black, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    )
    
    if (isLoading) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            onLoginSuccess()
        }
    }
}

@Composable
fun LoginButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF232323),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(28.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            icon()
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
