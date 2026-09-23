package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.LoginRepository
import com.example.tattle.models.Article
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.theme.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdatePreferences: (UserPreferences) -> Unit,
    onOpenArticle: (Article) -> Unit,
    onPurgeData: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val loginRepository = koinInject<LoginRepository>()

    // Profile state
    var name by remember(preferences) { mutableStateOf(preferences.name) }
    var dob by remember(preferences) { mutableStateOf(preferences.dob) }
    var phone by remember(preferences) { mutableStateOf(preferences.phoneNumber) }
    var email by remember(preferences) { mutableStateOf(preferences.email) }

    // Calculated detailed age from DOB (Years, Months, Days)
    val detailedAge = calculateDetailedAgeFromDob(dob)
    val calculatedAge = detailedAge?.years

    // Phone OTP Verification Dialog State
    var showPhoneOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var otpMessage by remember { mutableStateOf<String?>(null) }
    var profileSaveMessage by remember { mutableStateOf<String?>(null) }

    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) DarkSurface else Color(0xFFF5F5F5)
    val textColor = if (isDark) DarkTextPrimary else Color.Black
    val subTextColor = if (isDark) DarkTextMuted else Color.Gray

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(cardBg, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Settings",
                    color = textColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your profile, theme and notifications",
                    color = subTextColor,
                    fontSize = 13.sp
                )
            }
        }

        // 1. Theme Switcher Section
        SettingsSectionTitle(title = "APP THEME", color = subTextColor)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = if (isDark) "Dark Theme" else "Light Theme",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toggle between dark and light app mode",
                            color = subTextColor,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
                Switch(
                    checked = isDark,
                    onCheckedChange = { newMode ->
                        onUpdatePreferences(preferences.copy(isDarkMode = newMode))
                        scope.launch {
                            loginRepository.syncProfileWithServer(
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null },
                                isDarkMode = newMode
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }

        // 2. Notification Toggle Section
        SettingsSectionTitle(title = "NOTIFICATIONS", color = subTextColor)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = if (preferences.notifications.dailyBriefings) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            text = "Push Notifications",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enable or disable all breaking alerts &\ndaily briefings",
                            color = subTextColor,
                            fontSize = 12.sp,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }
                }
                Switch(
                    checked = preferences.notifications.dailyBriefings,
                    onCheckedChange = { enabled ->
                        onUpdatePreferences(
                            preferences.copy(
                                notifications = NotificationsPrefs(
                                    dailyBriefings = enabled,
                                    breakingAlerts = enabled,
                                    streaks = enabled
                                )
                            )
                        )
                        scope.launch {
                            loginRepository.syncProfileWithServer(
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null },
                                notificationsEnabled = enabled
                            )
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Primary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.LightGray
                    )
                )
            }
        }

        // 3. Edit Profile Section
        SettingsSectionTitle(title = "EDIT PROFILE", color = subTextColor)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = if (isDark) DarkSurface else Color.White,
                        unfocusedContainerColor = if (isDark) DarkSurface else Color.White
                    ),
                    singleLine = true
                )

                // DOB Input & Calculated Age Display
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {
                            if (it.length <= 8) {
                                dob = it.filter { c -> c.isDigit() }
                            }
                        },
                        label = { Text("Date of Birth (DD/MM/YYYY)") },
                        leadingIcon = { Icon(Icons.Default.Cake, null, tint = Primary) },
                        visualTransformation = DateVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = if (isDark) DarkSurface else Color.White,
                            unfocusedContainerColor = if (isDark) DarkSurface else Color.White
                        ),
                        singleLine = true
                    )

                    if (detailedAge != null) {
                        Text(
                            text = "Calculated Age: ${detailedAge.toFormattedString()}",
                            color = Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    } else if (dob.isNotEmpty() && dob.length == 8) {
                        Text(
                            text = "Invalid Date of Birth",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Phone Number with OTP Validation Button
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            focusedContainerColor = if (isDark) DarkSurface else Color.White,
                            unfocusedContainerColor = if (isDark) DarkSurface else Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedButton(
                        onClick = {
                            if (phone.isNotEmpty()) {
                                isVerifying = true
                                otpMessage = null
                                scope.launch {
                                    val res = loginRepository.generateOtp(phone)
                                    if (res.isSuccess) {
                                        isOtpSent = true
                                        showPhoneOtpDialog = true
                                    } else {
                                        otpMessage = res.exceptionOrNull()?.message ?: "Failed to send OTP"
                                    }
                                    isVerifying = false
                                }
                            } else {
                                otpMessage = "Please enter a valid phone number"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                    ) {
                        Icon(Icons.Default.VpnKey, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validate Mobile Number via OTP", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    if (otpMessage != null) {
                        Text(
                            text = otpMessage!!,
                            color = Primary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Email ID Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = if (isDark) DarkSurface else Color.White,
                        unfocusedContainerColor = if (isDark) DarkSurface else Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        val ageStr = calculatedAge?.toString() ?: preferences.ageGroup
                        onUpdatePreferences(
                            preferences.copy(
                                name = name,
                                dob = dob,
                                ageGroup = ageStr,
                                phoneNumber = phone,
                                email = email
                            )
                        )
                        scope.launch {
                            loginRepository.syncProfileWithServer(
                                name = name,
                                dob = dob,
                                age = calculatedAge,
                                phone = phone.ifBlank { null },
                                email = email.ifBlank { null },
                                isDarkMode = preferences.isDarkMode,
                                notificationsEnabled = preferences.notifications.dailyBriefings
                            )
                        }
                        profileSaveMessage = "Profile saved and synced with backend!"
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                if (profileSaveMessage != null) {
                    Text(
                        text = profileSaveMessage!!,
                        color = Color(0xFF2E7D32),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. Logout Button at Bottom
        Button(
            onClick = {
                onPurgeData()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary),
            shape = RoundedCornerShape(28.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Text(
            text = "TATTLE v3.1.0",
            color = subTextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Extra spacing so content is never blocked by bottom navigation bar
        Spacer(modifier = Modifier.height(100.dp))
    }

    // Phone OTP Dialog for Profile Editing Validation
    if (showPhoneOtpDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneOtpDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Verify Mobile Number", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("An OTP has been sent to $phone. Please enter it below:", fontSize = 14.sp, color = Color.DarkGray)
                    
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                        label = { Text("6-Digit OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    if (otpMessage != null) {
                        Text(otpMessage!!, color = Primary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput.length == 6) {
                            isVerifying = true
                            scope.launch {
                                val res = loginRepository.verifyOtp(phone, otpInput)
                                if (res.isSuccess) {
                                    onUpdatePreferences(preferences.copy(phoneNumber = phone))
                                    profileSaveMessage = "Phone number verified and updated!"
                                    showPhoneOtpDialog = false
                                } else {
                                    otpMessage = res.exceptionOrNull()?.message ?: "Invalid OTP"
                                }
                                isVerifying = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Verify & Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhoneOtpDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionTitle(title: String, color: Color) {
    Text(
        text = title,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}
