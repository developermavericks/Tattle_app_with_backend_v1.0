package com.example.tattle.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import com.example.tattle.ui.components.Appear
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.data.UserSync
import com.example.tattle.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** What the username box is saying right now. */
sealed interface UsernameStatus {
    data object Idle : UsernameStatus
    data object Checking : UsernameStatus
    data class Available(val message: String) : UsernameStatus
    data class Problem(val message: String) : UsernameStatus
}

/** The state of the "name and username" step (shared by onboarding and the one-time prompt for people who joined before usernames existed). */
class UsernameForm(initialName: String = "") {
    var name by mutableStateOf(initialName)
    var username by mutableStateOf("")
    var status by mutableStateOf<UsernameStatus>(UsernameStatus.Idle)
    var alternatives by mutableStateOf<List<String>>(emptyList())
    var editedByHand by mutableStateOf(false)          // once the person types their own, stop replacing it with suggestions
    var saving by mutableStateOf(false)
    var refresh by mutableStateOf(0)                   // "Suggest another" bumps this to ask for new suggestions

    val ready: Boolean get() = status is UsernameStatus.Available && name.trim().length >= 2 && !saving

    /** Reserves the username on the server. False (with the reason shown in the box) when it was just taken or the server is unreachable. */
    suspend fun claim(userSync: UserSync): Boolean {
        saving = true
        val res = userSync.claimUsername(username, name.trim())
        saving = false
        if (res == null) {
            status = UsernameStatus.Problem("No connection. Please try again.")
            return false
        }
        if (!res.success) {
            status = UsernameStatus.Problem(res.message ?: "That username is not available.")
            if (res.alternatives.isNotEmpty()) alternatives = res.alternatives
            return false
        }
        return true
    }
}

@Composable
fun UsernameStep(form: UsernameForm, userSync: UserSync) {
    // a free username is suggested from the name as soon as the person has typed it
    LaunchedEffect(form.name, form.refresh) {
        if (!form.editedByHand && form.name.trim().length >= 2) {
            delay(600)
            val r = userSync.suggestUsername(form.name.trim())
            if (r != null && !form.editedByHand) {
                r.suggested?.let { form.username = it }
                form.alternatives = r.alternatives
            }
        }
    }
    // and whatever is in the box is checked for availability
    LaunchedEffect(form.username) {
        val u = form.username
        if (u.length < 3) {
            form.status = UsernameStatus.Idle
            return@LaunchedEffect
        }
        form.status = UsernameStatus.Checking
        delay(450)
        val r = userSync.checkUsername(u)
        form.status = when {
            r == null -> UsernameStatus.Problem("Could not check right now. Check your connection.")
            r.available -> UsernameStatus.Available(r.message.ifBlank { "Available!" })
            else -> UsernameStatus.Problem(r.message.ifBlank { "Not available." })
        }
        if (r != null && !r.available && r.alternatives.isNotEmpty()) form.alternatives = r.alternatives
    }

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What should we call you?",
            color = Color.Black,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Tell us your name and pick a username that is only yours.",
            color = Color.Gray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "YOUR NAME",
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = form.name,
            onValueChange = { if (it.length <= 40) form.name = it },
            textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.Black),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, keyboardType = KeyboardType.Text),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = underlineFieldColors(),
            placeholder = {
                Text("Your name", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFFCCCCCC), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        )

        Spacer(Modifier.height(30.dp))

        Text(
            text = "YOUR USERNAME",
            color = Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        TextField(
            value = form.username,
            onValueChange = { raw ->
                val cleaned = raw.lowercase().filter { c -> c in 'a'..'z' || c in '0'..'9' || c == '.' || c == '_' }.take(20)
                form.editedByHand = true
                form.username = cleaned
            },
            textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, color = Color.Black),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, keyboardType = KeyboardType.Ascii),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.9f),
            colors = underlineFieldColors(),
            prefix = { Text("@", color = Primary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold) },
            placeholder = {
                Text("username", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = Color(0xFFCCCCCC), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        )

        Spacer(Modifier.height(10.dp))
        Crossfade(targetState = form.status, animationSpec = tween(180), label = "usernameStatus") { s ->
        Row(modifier = Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            when (s) {
                UsernameStatus.Idle -> Text("3 to 20 letters, numbers, dots or underscores", color = Color.Gray, fontSize = 13.sp)
                UsernameStatus.Checking -> {
                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Primary)
                    Text("Checking...", color = Color.Gray, fontSize = 13.sp)
                }
                is UsernameStatus.Available -> {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Text(s.message, color = Color(0xFF2E7D32), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                is UsernameStatus.Problem -> {
                    Icon(Icons.Default.ErrorOutline, null, tint = Color(0xFFB71C1C), modifier = Modifier.size(18.dp))
                    Text(s.message, color = Color(0xFFB71C1C), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        }

        if (form.alternatives.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Text("OR TRY ONE OF THESE", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                form.alternatives.filter { it != form.username }.forEachIndexed { index, alt ->
                    Appear(index * 60) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.border(1.dp, Color(0xFFE8E8E8), RoundedCornerShape(20.dp)).clickable {
                            form.editedByHand = true
                            form.username = alt
                        }
                    ) {
                        Text("@$alt", modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    }
                }
            }
        }

        TextButton(
            onClick = {
                form.editedByHand = false
                form.username = ""
                form.refresh++                                      // asks for fresh suggestions for the same name
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Icon(Icons.Default.Refresh, null, tint = Primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Suggest another", color = Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun underlineFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Black,
    unfocusedIndicatorColor = Color(0xFFE8E8E8),
    cursorColor = Primary
)

/** Shown once to people who joined before usernames existed: same step, one button. */
@Composable
fun UsernameGateScreen(initialName: String, onDone: (name: String, username: String) -> Unit) {
    val userSync = org.koin.compose.koinInject<UserSync>()
    val form = remember { UsernameForm(initialName) }
    val scope = rememberCoroutineScope()
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Button(
                    onClick = { scope.launch { if (form.claim(userSync)) onDone(form.name.trim(), form.username) } },
                    enabled = form.ready,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF232323), contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE8E8E8), disabledContentColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(28.dp)
                ) { Text("Continue", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) { UsernameStep(form, userSync) }
    }
}
