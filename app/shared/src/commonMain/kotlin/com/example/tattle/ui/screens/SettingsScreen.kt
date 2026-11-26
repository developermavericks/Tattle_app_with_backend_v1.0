package com.example.tattle.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.LoginRepository
import com.example.tattle.models.Article
import com.example.tattle.models.NotificationsPrefs
import com.example.tattle.models.UserPreferences
import com.example.tattle.ui.components.appearOnEnter
import com.example.tattle.ui.components.pressScale
import com.example.tattle.ui.theme.*
import com.example.tattle.utils.Haptics
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/** Shown at the bottom of Settings. Keep in step with versionName in androidApp/build.gradle.kts. */
private const val APP_VERSION = "1.0"

/**
 * Settings, grouped by what a reader actually changes:
 *   profile summary -> Reading (theme, trendy rewrite) -> Notifications (one switch per kind) -> Privacy (city) -> Account details -> Log out.
 * Every row has the same layout (icon, title, one-line help, control), so nothing looks out of place.
 */
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
    val userSync = koinInject<com.example.tattle.data.UserSync>()

    // Account fields
    var phone by remember(preferences) { mutableStateOf(preferences.phoneNumber) }
    var email by remember(preferences) { mutableStateOf(preferences.email) }
    var accountOpen by remember { mutableStateOf(false) }
    val detailedAge = calculateDetailedAgeFromDob(preferences.dob)
    val calculatedAge = detailedAge?.years
    val accountChanged = email != preferences.email
    val phoneNeedsCheck = phone.isNotBlank() && phone != preferences.phoneNumber

    // The reading streak shown here must match the Recap screen: both come from the server's live day-count (GET /api/user/recap),
    // never from preferences.streak, which is only ever set once at onboarding and is never updated again.
    var streakDays by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(Unit) { streakDays = userSync.fetchRecap()?.streakDays }

    // Phone OTP dialog
    var showPhoneOtpDialog by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var otpMessage by remember { mutableStateOf<String?>(null) }
    var profileSaveMessage by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteConfirm by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    val isDark = preferences.isDarkMode
    val bgColor = if (isDark) DarkBackground else Color.White
    val cardBg = if (isDark) DarkSurface else Color(0xFFF5F5F5)
    val fieldBg = if (isDark) DarkSurfaceDim else Color.White
    val textColor = if (isDark) DarkTextPrimary else Color.Black
    val subTextColor = if (isDark) DarkTextMuted else Color(0xFF6B6B6B)

    fun updateNotifications(next: NotificationsPrefs) {
        onUpdatePreferences(preferences.copy(notifications = next))
        scope.launch {
            loginRepository.syncProfileWithServer(
                phone = phone.ifBlank { null },
                email = email.ifBlank { null },
                notificationsEnabled = next.breakingAlerts || next.dailyBriefings || next.streaks
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(bgColor).verticalScroll(scrollState).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).background(cardBg, RoundedCornerShape(12.dp))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
            }
            Text("Settings", color = textColor, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        // Who is signed in
        Row(
            modifier = Modifier.appearOnEnter(0).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cardBg).clickable { Haptics.selection(); accountOpen = true }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val shownName = preferences.name.ifBlank { preferences.username.ifBlank { "Reader" } }
            Box(modifier = Modifier.size(52.dp).background(Primary, CircleShape), contentAlignment = Alignment.Center) {
                Text(shownName.first().uppercase(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(shownName, color = textColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                val line = listOfNotNull(
                    preferences.username.takeIf { it.isNotBlank() }?.let { "@$it" },
                    calculatedAge?.let { "$it yrs" }
                ).joinToString("  -  ")
                if (line.isNotBlank()) Text(line, color = subTextColor, fontSize = 13.sp)
            }
            streakDays?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                    Text("$it", color = Primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ---- Reading
        SettingsSectionTitle("READING", subTextColor)
        SettingsGroup(cardBg, 1) {
            SettingRow(Icons.Default.DarkMode, "Dark mode", null, textColor, subTextColor) {
                ThemeSwitch(isDark) { on ->
                    onUpdatePreferences(preferences.copy(isDarkMode = on))
                    scope.launch { loginRepository.syncProfileWithServer(phone = phone.ifBlank { null }, email = email.ifBlank { null }, isDarkMode = on) }
                }
            }
            SettingDivider(subTextColor)
            SettingRow(Icons.Default.AutoAwesome, "Trendy rewrite", "Newspaper stories in a casual, Gen Z voice", textColor, subTextColor) {
                SettingSwitch(preferences.genZMode) { on -> onUpdatePreferences(preferences.copy(genZMode = on)) }
            }
        }

        // ---- Notifications (one switch for each kind)
        SettingsSectionTitle("NOTIFICATIONS", subTextColor)
        SettingsGroup(cardBg, 2) {
            val n = preferences.notifications
            SettingRow(Icons.Default.Bolt, "Breaking news", null, textColor, subTextColor) {
                SettingSwitch(n.breakingAlerts) { on -> updateNotifications(n.copy(breakingAlerts = on)) }
            }
            SettingDivider(subTextColor)
            SettingRow(Icons.Default.Headphones, "Daily briefing", null, textColor, subTextColor) {
                SettingSwitch(n.dailyBriefings) { on -> updateNotifications(n.copy(dailyBriefings = on)) }
            }
            SettingDivider(subTextColor)
            SettingRow(Icons.Default.LocalFireDepartment, "Streak reminders", null, textColor, subTextColor) {
                SettingSwitch(n.streaks) { on -> updateNotifications(n.copy(streaks = on)) }
            }
        }

        // ---- Privacy
        SettingsSectionTitle("PRIVACY", subTextColor)
        SettingsGroup(cardBg, 3) {
            var shareLocation by remember { mutableStateOf(userSync.locationChoice == "yes") }
            SettingRow(
                Icons.Default.LocationOn, "Share my city",
                if (shareLocation) (userSync.locationCity?.let { "Showing news near $it" } ?: "Used to show news near you") else "Off. Turn on for news near you.",
                textColor, subTextColor
            ) {
                SettingSwitch(shareLocation) { on ->
                    shareLocation = on
                    scope.launch { if (on) { if (!userSync.enableLocation()) shareLocation = false } else userSync.disableLocation() }
                }
            }
        }

        // ---- Account details (collapsed until needed)
        SettingsSectionTitle("ACCOUNT", subTextColor)
        SettingsGroup(cardBg, 4) {
            SettingRow(
                Icons.Default.Person, "Account details",
                if (accountOpen) "Phone and email" else listOfNotNull(
                    preferences.email.takeIf { it.isNotBlank() }, preferences.phoneNumber.takeIf { it.isNotBlank() }
                ).firstOrNull() ?: "Add your phone and email",
                textColor, subTextColor, modifier = Modifier.clickable { Haptics.selection(); accountOpen = !accountOpen }
            ) {
                Icon(if (accountOpen) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = subTextColor)
            }
            AnimatedVisibility(visible = accountOpen) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    @Composable
                    fun fieldColors() = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = if (isDark) Color(0xFF444444) else Color(0xFFE0E0E0),
                        focusedContainerColor = fieldBg, unfocusedContainerColor = fieldBg,
                        focusedTextColor = textColor, unfocusedTextColor = textColor
                    )
                    if (preferences.dob.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Default.Cake, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Text(
                                calculatedAge?.let { "Date of birth on file - $it years old" } ?: "Date of birth on file",
                                color = subTextColor, fontSize = 13.sp
                            )
                        }
                    }
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it }, label = { Text("Phone number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors(), singleLine = true
                    )
                    if (phoneNeedsCheck) {
                        OutlinedButton(
                            onClick = {
                                isVerifying = true
                                otpMessage = null
                                scope.launch {
                                    val res = loginRepository.generateOtp(phone)
                                    if (res.isSuccess) showPhoneOtpDialog = true else otpMessage = res.exceptionOrNull()?.message ?: "Could not send the code"
                                    isVerifying = false
                                }
                            },
                            enabled = !isVerifying,
                            modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Primary)
                        ) {
                            Icon(Icons.Default.VpnKey, null, tint = Primary, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (isVerifying) "Sending code..." else "Verify this number", color = Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    if (otpMessage != null && !showPhoneOtpDialog) Text(otpMessage!!, color = Primary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = email, onValueChange = { email = it }, label = { Text("Email address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = fieldColors(), singleLine = true
                    )
                    Button(
                        onClick = {
                            onUpdatePreferences(preferences.copy(email = email))
                            scope.launch {
                                loginRepository.syncProfileWithServer(
                                    phone = preferences.phoneNumber.ifBlank { null }, email = email.ifBlank { null },
                                    isDarkMode = preferences.isDarkMode,
                                    notificationsEnabled = preferences.notifications.let { it.breakingAlerts || it.dailyBriefings || it.streaks }
                                )
                            }
                            profileSaveMessage = "Saved"
                        },
                        enabled = accountChanged,
                        modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) { Text("Save changes", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    if (profileSaveMessage != null && !accountChanged) {
                        Text(profileSaveMessage!!, color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // ---- Log out (quiet, with a confirmation)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { Haptics.medium(); showLogoutDialog = true },
            modifier = Modifier.pressScale().fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Primary)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Log out", color = Primary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        TextButton(
            onClick = { Haptics.warning(); deleteConfirm = ""; deleteError = null; showDeleteDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DeleteForever, null, tint = Color(0xFFB3261E), modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Delete account", color = Color(0xFFB3261E), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
        Text("Tattle $APP_VERSION", color = subTextColor, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))

        // room for the bottom navigation bar
        Spacer(Modifier.height(96.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Log out?", fontWeight = FontWeight.Bold, color = textColor) },
            text = { Text("You will be signed out on this phone and the stories saved here will be cleared.", color = subTextColor) },
            confirmButton = {
                Button(onClick = { Haptics.success(); showLogoutDialog = false; onPurgeData(); onBack() }, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Log out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = subTextColor) } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!deleting) showDeleteDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Delete your account?", fontWeight = FontWeight.Bold, color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Your account, reading history, saved stories and settings are deleted. You will need to sign up again to use Tattle.",
                        color = subTextColor, fontSize = 14.sp
                    )
                    OutlinedTextField(
                        value = deleteConfirm, onValueChange = { deleteConfirm = it.take(10) }, singleLine = true, enabled = !deleting,
                        label = { Text("Type DELETE to confirm") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB3261E), unfocusedBorderColor = if (isDark) Color(0xFF444444) else Color(0xFFE0E0E0),
                            focusedTextColor = textColor, unfocusedTextColor = textColor
                        )
                    )
                    if (deleteError != null) Text(deleteError!!, color = Color(0xFFB3261E), fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    enabled = deleteConfirm.trim().equals("DELETE", ignoreCase = true) && !deleting,
                    onClick = {
                        deleting = true
                        deleteError = null
                        scope.launch {
                            if (userSync.deleteAccount()) {
                                Haptics.heavy()
                                showDeleteDialog = false
                                onPurgeData()
                                onBack()
                            } else {
                                Haptics.warning()
                                deleteError = "Could not delete the account. Check your connection and try again."
                            }
                            deleting = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) { Text(if (deleting) "Deleting..." else "Delete account", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(enabled = !deleting, onClick = { showDeleteDialog = false }) { Text("Cancel", color = subTextColor) } }
        )
    }

    // Phone OTP dialog
    if (showPhoneOtpDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneOtpDialog = false },
            containerColor = cardBg,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Verify mobile number", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("A code was sent to $phone. Enter it below:", fontSize = 14.sp, color = subTextColor)
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 6) otpInput = it.filter { c -> c.isDigit() } },
                        label = { Text("6-digit code") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    if (otpMessage != null) Text(otpMessage!!, color = Primary, fontSize = 12.sp)
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
                                    profileSaveMessage = "Phone number verified"
                                    otpInput = ""
                                    showPhoneOtpDialog = false
                                } else {
                                    otpMessage = res.exceptionOrNull()?.message ?: "That code is not right"
                                }
                                isVerifying = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Verify and save", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showPhoneOtpDialog = false }) { Text("Cancel", color = subTextColor) } }
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
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp)
    )
}

@Composable
private fun SettingsGroup(background: Color, index: Int = 0, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.appearOnEnter(index).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(background), content = content)
}

@Composable
private fun SettingDivider(color: Color) {
    HorizontalDivider(modifier = Modifier.padding(start = 60.dp, end = 16.dp), color = color.copy(alpha = 0.18f))
}

/** One row everywhere: icon, title, one line of help, and a control on the right. */
@Composable
private fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    textColor: Color,
    subTextColor: Color,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(26.dp))
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (!subtitle.isNullOrBlank()) Text(subtitle, color = subTextColor, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun SettingSwitch(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = { Haptics.selection(); onChange(it) },
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White, checkedTrackColor = Primary,
            uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFFBDBDBD), uncheckedBorderColor = Color.Transparent
        )
    )
}

/** The dark-mode switch: a white thumb that slides across a red (dark mode on) or grey (off) track, with a moon or sun
 * icon sitting in the track on the side the thumb has left uncovered. */
@Composable
private fun ThemeSwitch(isDark: Boolean, onChange: (Boolean) -> Unit) {
    val trackColor by animateColorAsState(if (isDark) Primary else Color(0xFFBDBDBD), label = "track")
    val thumbOffset by animateDpAsState(if (isDark) 24.dp else 2.dp, label = "thumb")
    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .clickable { Haptics.selection(); onChange(!isDark) }
    ) {
        Icon(
            imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightlightRound,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(if (isDark) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 6.dp)
                .size(14.dp)
        )
        Box(
            modifier = Modifier
                .padding(2.dp)
                .offset(x = thumbOffset)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
