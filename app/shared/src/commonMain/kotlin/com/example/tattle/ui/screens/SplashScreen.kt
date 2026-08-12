package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
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
    var showPhoneLogin by remember { mutableStateOf(false) }
    var showEmailLogin by remember { mutableStateOf(false) }
    
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
            modifier = Modifier.fillMaxSize().padding(top = 40.dp),
            contentScale = ContentScale.FillWidth,
            alpha = 1.0f,
            colorFilter = ColorFilter.tint(Primary.copy(alpha = 0.8f))
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
                        onClick = { showPhoneLogin = true }
                    )

                    LoginButton(
                        text = "Continue with Email",
                        icon = {
                            Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.padding(end = 8.dp))
                        },
                        onClick = { showEmailLogin = true }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "By continuing, you agree to our Terms of Service",
                        color = Color.LightGray,
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

        if (showPhoneLogin) {
            PhoneLoginOverlay(
                onDismiss = { showPhoneLogin = false },
                onLoginSuccess = {
                    showPhoneLogin = false
                    onGetStarted()
                }
            )
        }

        if (showEmailLogin) {
            EmailLoginOverlay(
                onDismiss = { showEmailLogin = false },
                onLoginSuccess = {
                    showEmailLogin = false
                    onGetStarted()
                }
            )
        }
    }
}

@Composable
fun PhoneLoginOverlay(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val masterOtp = "951753"

    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceScale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        modifier = Modifier.clip(RoundedCornerShape(32.dp)),
        title = {
            Text(
                if (!isOtpSent) "Continue with Phone" else "Verify your number",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            var contentVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { contentVisible = true }
            
            AnimatedVisibility(
                visible = contentVisible,
                enter = scaleIn(tween(500)) + fadeIn(tween(500)),
                exit = scaleOut(tween(500)) + fadeOut(tween(500))
            ) {
                AnimatedContent(
                    targetState = isOtpSent,
                    transitionSpec = {
                        if (targetState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "PhoneStepAnimation"
                ) { stepIsOtpSent ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        if (isLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                                Text(
                                    "Authenticating...",
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            if (!stepIsOtpSent) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Simple Country Code Picker (UI only)
                                    Card(
                                        modifier = Modifier.height(56.dp).width(70.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(countryCode, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { 
                                            val digits = it.filter { char -> char.isDigit() }
                                            if (digits.length <= 10) {
                                                phoneNumber = digits
                                                error = null
                                                // Simple auto-identification simulation
                                                countryCode = when {
                                                    digits.startsWith("1") -> "+1"
                                                    digits.startsWith("44") -> "+44"
                                                    else -> "+91"
                                                }
                                            }
                                        },
                                        label = { Text("10-digit Number") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Primary,
                                            unfocusedBorderColor = Color(0xFFE8E8E8),
                                            focusedContainerColor = Color(0xFFF5F5F5),
                                            unfocusedContainerColor = Color(0xFFF5F5F5),
                                            focusedLabelColor = Primary,
                                            unfocusedLabelColor = Color.Gray
                                        ),
                                        singleLine = true
                                    )
                                }
                                
                                AnimatedVisibility(
                                    visible = error != null,
                                    enter = slideInVertically() + fadeIn(),
                                    exit = slideOutVertically() + fadeOut()
                                ) {
                                    Text(
                                        error ?: "",
                                        color = Primary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                val isReady = phoneNumber.length == 10
                                Button(
                                    onClick = { 
                                        if (isReady) {
                                            isOtpSent = true 
                                        } else {
                                            error = "Please enter exactly 10 digits"
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .graphicsLayer {
                                            if (isReady) {
                                                scaleX = bounceScale
                                                scaleY = bounceScale
                                            }
                                        },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isReady) Color(0xFF232323) else Color(0xFFE8E8E8)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Send Code", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            } else {
                                Text(
                                    "Enter the code sent to $countryCode $phoneNumber",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                            val focusRequester = remember { FocusRequester() }
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }

                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                BasicTextField(
                                    value = TextFieldValue(otp, selection = TextRange(otp.length)),
                                    onValueChange = {
                                        if (it.text.length <= 6) {
                                            otp = it.text.filter { char -> char.isDigit() }
                                            error = null
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.focusRequester(focusRequester).size(1.dp).graphicsLayer { alpha = 0f }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                                ) {
                                    repeat(6) { index ->
                                        val char = otp.getOrNull(index)?.toString() ?: ""
                                        val isFocused = otp.length == index
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (isFocused) Primary else Color(0xFFE8E8E8),
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                .background(Color(0xFFF5F5F5), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = char,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                                
                                AnimatedVisibility(
                                    visible = error != null,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Text(
                                        error ?: "",
                                        color = Primary,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(top = 8.dp, start = 8.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                val isOtpReady = otp.length == 6
                                Button(
                                    onClick = { 
                                        if (otp == masterOtp) {
                                            isLoading = true 
                                        } else {
                                            error = "Invalid code. Try 951753"
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .graphicsLayer {
                                            if (isOtpReady) {
                                                scaleX = bounceScale
                                                scaleY = bounceScale
                                            }
                                        },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isOtpReady) Primary else Color(0xFFE8E8E8)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                
                                TextButton(
                                    onClick = { isOtpSent = false },
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                                ) {
                                    Text("Edit Number", color = Color.Gray, fontSize = 14.sp)
                                }
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
fun EmailLoginOverlay(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    val masterEmail = "developerteam@themavericksindia.com"
    val masterPassword = "12345"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        title = {
            Text(
                "Continue with Email",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Primary)
                        Text("Logging in...", modifier = Modifier.padding(top = 16.dp), color = Color.Gray)
                    }
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            error = null
                        },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE8E8E8),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            error = null
                        },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE8E8E8),
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            focusedLabelColor = Primary,
                            unfocusedLabelColor = Color.Gray
                        ),
                        singleLine = true
                    )
                    if (error != null) {
                        Text(error!!, color = Primary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { 
                            if (email == masterEmail && password == masterPassword) {
                                isLoading = true 
                            } else {
                                error = "Invalid email or password"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF232323)),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Text("Login", fontWeight = FontWeight.Bold)
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
            containerColor = Primary,
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
