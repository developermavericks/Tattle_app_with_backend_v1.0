package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.auth.signInWithCode
import com.example.tattle.auth.verifyPhoneNumber
import com.example.tattle.ui.theme.LocalAppLanguage
import com.example.tattle.ui.theme.LocalStrings
import com.example.tattle.ui.theme.Primary
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.SupabaseClient
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onGetStarted: () -> Unit) {
    var showGoogleLogin by remember { mutableStateOf(false) }
    var showPhoneLogin by remember { mutableStateOf(false) }
    var showEmailLogin by remember { mutableStateOf(false) }
    val language = LocalAppLanguage.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Primary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = LocalStrings.get("app_name", language),
                color = Color.White,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = LocalStrings.get("your_neighborhood", language),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            LoginButton(
                text = LocalStrings.get("continue_google", language),
                icon = { Icon(Icons.Default.Email, null, tint = Color.White, modifier = Modifier.padding(end = 8.dp)) },
                onClick = { showGoogleLogin = true }
            )
            
            LoginButton(
                text = LocalStrings.get("continue_phone", language),
                icon = { Icon(Icons.Default.Phone, null, tint = Color.White, modifier = Modifier.padding(end = 8.dp)) },
                onClick = { showPhoneLogin = true }
            )
            
            LoginButton(
                text = LocalStrings.get("continue_email", language),
                icon = null,
                onClick = { showEmailLogin = true }
            )
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
    val language = LocalAppLanguage.current
    var phoneNumber by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var verificationId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

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
                if (!isOtpSent) LocalStrings.get("continue_phone", language) else LocalStrings.get("verify_number", language),
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
                                    LocalStrings.get("authenticating", language),
                                    modifier = Modifier.padding(top = 16.dp),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            if (!stepIsOtpSent) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                            isLoading = true
                                            scope.launch {
                                                verifyPhoneNumber(
                                                    phoneNumber = "$countryCode$phoneNumber",
                                                    onCodeSent = { id ->
                                                        verificationId = id
                                                        isOtpSent = true
                                                        isLoading = false
                                                    },
                                                    onError = { msg ->
                                                        error = msg
                                                        isLoading = false
                                                    }
                                                )
                                            }
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
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isReady) Color(0xFF232323) else Color(0xFFE8E8E8)),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(LocalStrings.get("send_code", language), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Text(
                                    "${LocalStrings.get("enter_code_sent", language)} $countryCode $phoneNumber",
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
                                        if (isOtpReady && (verificationId != null)) {
                                            isLoading = true
                                            scope.launch {
                                                signInWithCode(
                                                    verificationId = verificationId!!,
                                                    code = otp,
                                                    onSuccess = {
                                                        isLoading = false
                                                        onLoginSuccess()
                                                    },
                                                    onError = { msg ->
                                                        error = msg
                                                        isLoading = false
                                                    }
                                                )
                                            }
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
                                        containerColor = if (isOtpReady) Color(0xFF232323) else Color(0xFFE8E8E8)
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(LocalStrings.get("verify_continue", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                
                                TextButton(
                                    onClick = { isOtpSent = false },
                                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                                ) {
                                    Text(LocalStrings.get("edit_number", language), color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun EmailLoginOverlay(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    val supabase = koinInject<SupabaseClient>()
    val language = LocalAppLanguage.current
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        modifier = Modifier.clip(RoundedCornerShape(32.dp)),
        title = {
            Text(
                if (!isOtpSent) LocalStrings.get("continue_email", language) else LocalStrings.get("verify_login", language),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        },
        text = {
            AnimatedContent(
                targetState = isOtpSent,
                transitionSpec = {
                    if (targetState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "EmailStepAnimation"
            ) { stepIsOtpSent ->
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = Primary, strokeWidth = 3.dp)
                        }
                    } else {
                        if (!stepIsOtpSent) {
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it; error = null },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Color(0xFFE8E8E8)
                                )
                            )
                            
                            if (error != null) {
                                Text(error!!, color = Primary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val isReady = email.contains("@") && email.contains(".")
                            Button(
                                onClick = { 
                                    if (isReady) {
                                        isLoading = true
                                        scope.launch {
                                            try {
                                                supabase.auth.signInWith(OTP) {
                                                    this.email = email
                                                }
                                                isOtpSent = true
                                            } catch (e: Exception) {
                                                error = "Failed to send code: ${e.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    } else {
                                        error = "Please enter a valid email"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(LocalStrings.get("magic_code", language), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                "${LocalStrings.get("enter_code_sent", language)} $email",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            val focusRequester = remember { FocusRequester() }
                            LaunchedEffect(Unit) { focusRequester.requestFocus() }

                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    repeat(6) { index ->
                                        val char = otp.getOrNull(index)?.toString() ?: ""
                                        Box(
                                            modifier = Modifier.size(42.dp).border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(8.dp)).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(char, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            if (error != null) {
                                Text(error!!, color = Primary, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            val isOtpReady = otp.length == 6
                            Button(
                                onClick = { 
                                    if (isOtpReady) {
                                        isLoading = true
                                        scope.launch {
                                            try {
                                                supabase.auth.verifyEmailOtp(
                                                    type = OtpType.Email.EMAIL,
                                                    email = email,
                                                    token = otp
                                                )
                                                onLoginSuccess()
                                            } catch (e: Exception) {
                                                error = "Invalid code: ${e.message}"
                                            } finally {
                                                isLoading = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text(LocalStrings.get("verify_login", language), fontWeight = FontWeight.Bold)
                            }
                            
                            TextButton(onClick = { isOtpSent = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(LocalStrings.get("change_email", language), color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun GoogleLoginOverlay(onDismiss: () -> Unit, onLoginSuccess: () -> Unit) {
    var isLoading by remember { mutableStateOf(false) }
    val language = LocalAppLanguage.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        modifier = Modifier.clip(RoundedCornerShape(32.dp)),
        title = {
            Text(LocalStrings.get("google_login", language), color = Color.Black, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Primary)
                } else {
                    Text("Simulating Google OAuth flow...", color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { isLoading = true },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(LocalStrings.get("sign_in_google", language), color = Color.White)
                    }
                }
            }
        }
    )

    if (isLoading) {
        LaunchedEffect(Unit) {
            delay(1500)
            onLoginSuccess()
        }
    }
}

@Composable
fun LoginButton(text: String, icon: (@Composable () -> Unit)?, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            icon?.invoke()
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}
