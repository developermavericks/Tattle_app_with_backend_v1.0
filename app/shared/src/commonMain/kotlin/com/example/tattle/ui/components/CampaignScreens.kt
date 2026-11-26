package com.example.tattle.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tattle.PlatformConfig
import com.example.tattle.data.CampaignDto
import com.example.tattle.utils.AdVideoPlayer
import com.example.tattle.utils.PlayableAdView
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.QuestionDto
import com.example.tattle.data.UserSync
import com.example.tattle.ui.theme.DarkSurface
import com.example.tattle.ui.theme.Primary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.koin.compose.koinInject

/** An ad switched on in the admin dashboard: picture, headline, text, a button, and a countdown the admin sets before it can be closed. */
@Composable
fun ServerAdDialog(
    campaign: CampaignDto,
    onAdCompleted: () -> Unit,
    onDismiss: () -> Unit
) {
    val userSync = koinInject<UserSync>()
    val images = koinInject<ImageRepository>()
    val uriHandler = LocalUriHandler.current
    val total = campaign.durationSeconds.coerceIn(1, 60)
    var secondsRemaining by remember(campaign.id) { mutableIntStateOf(total) }
    val finished = secondsRemaining <= 0
    var picture by remember(campaign.id) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(campaign.id) {
        userSync.campaignEvent(campaign.id, "impression")
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }
    LaunchedEffect(campaign.imageUrl) {
        campaign.imageUrl?.let { picture = images.fetchBitmapFromUrl(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.92f).padding(16.dp).popIn(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5252)))
                        Text("SPONSORED", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                    Surface(
                        color = Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Primary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (!finished) "0:${secondsRemaining.toString().padStart(2, '0')}" else "DONE",
                            color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                val mediaAddress = campaign.mediaUrl?.takeIf { it.isNotBlank() }?.let { if (it.startsWith("/")) PlatformConfig.BASE_URL.trimEnd('/') + it else it }
                fun openStore(address: String?) {
                    userSync.campaignEvent(campaign.id, "click")
                    try { (address ?: campaign.ctaUrl)?.let { uriHandler.openUri(it) } } catch (_: Exception) { }      // a Play Store link opens the Play Store
                }
                when {
                    campaign.adFormat == "video" && mediaAddress != null ->
                        Box(modifier = Modifier.fillMaxWidth().height(230.dp).clip(RoundedCornerShape(24.dp)).background(Color.Black)) {
                            AdVideoPlayer(mediaAddress, Modifier.fillMaxSize())
                        }
                    campaign.adFormat == "playable" && mediaAddress != null ->
                        Box(modifier = Modifier.fillMaxWidth().height(400.dp).clip(RoundedCornerShape(24.dp)).background(Color(0xFF14161C))) {
                            PlayableAdView(mediaAddress, Modifier.fillMaxSize()) { address -> openStore(address) }
                        }
                    else -> picture?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(24.dp))
                        )
                    }
                }

                Text(campaign.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                campaign.body?.takeIf { it.isNotBlank() }?.let {
                    Text(it, color = Color(0xFFCCCCCC), fontSize = 14.sp, lineHeight = 20.sp, textAlign = TextAlign.Center)
                }

                if (!campaign.ctaUrl.isNullOrBlank()) {
                    val rich = campaign.adFormat != "image"                           // video and game ads: a filled install button
                    val ctaLabel = campaign.ctaLabel?.takeIf { it.isNotBlank() } ?: if (rich) "Install" else "Learn more"
                    if (rich) {
                        Button(
                            onClick = { openStore(null) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) { Text(ctaLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    } else {
                        OutlinedButton(
                            onClick = { openStore(null) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
                        ) { Text(ctaLabel, color = Primary, fontWeight = FontWeight.Bold) }
                    }
                }

                LinearProgressIndicator(
                    progress = { (total - secondsRemaining).toFloat() / total },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = Primary,
                    trackColor = Color(0xFF333333)
                )

                Button(
                    onClick = {
                        userSync.campaignEvent(campaign.id, "dismiss")
                        onAdCompleted()                                   // watching to the end unlocks the 1-hour pass
                    },
                    enabled = finished,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = Color(0xFF3A3A3A))
                ) {
                    Text(if (finished) "Continue reading ⚡" else "Please wait $secondsRemaining s", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

/** A survey switched on in the admin dashboard: multiple choice (pick one), check boxes (pick several), polls (pick one, then see how everyone voted), rating, free text. */
@Composable
fun ServerSurveyCard(
    campaign: CampaignDto,
    onComplete: () -> Unit,
    onSkip: () -> Unit,
    isDark: Boolean = false,
    modifier: Modifier = Modifier,
    grantsPass: Boolean = true
) {
    val userSync = koinInject<UserSync>()
    val scope = rememberCoroutineScope()
    val questions = campaign.questions
    var index by remember(campaign.id) { mutableIntStateOf(0) }
    val answers = remember(campaign.id) { mutableStateMapOf<String, JsonElement>() }
    var singlePick by remember(campaign.id, index) { mutableStateOf<String?>(null) }
    var multiPick by remember(campaign.id, index) { mutableStateOf<Set<String>>(emptySet()) }
    var rating by remember(campaign.id, index) { mutableIntStateOf(0) }
    var text by remember(campaign.id, index) { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }
    var pollResults by remember(campaign.id) { mutableStateOf<Map<String, Map<String, Int>>>(emptyMap()) }
    val hasPoll = questions.any { it.type == "poll" }

    LaunchedEffect(campaign.id) { userSync.campaignEvent(campaign.id, "impression") }

    val bgColor = if (isDark) DarkSurface else Color.White
    val textColor = if (isDark) Color.White else Color.Black
    val optionBg = if (isDark) Color(0xFF282828) else Color(0xFFF5F5F5)
    val q: QuestionDto? = questions.getOrNull(index)
    val last = index >= questions.size - 1
    val canGoOn = when (q?.type) {
        "single", "poll" -> singlePick != null
        "multi" -> multiPick.isNotEmpty()
        "rating" -> rating > 0
        else -> true                                         // a free-text answer is optional
    }

    fun saveAnswer() {
        val current = q ?: return
        when (current.type) {
            "single", "poll" -> singlePick?.let { answers[current.id] = JsonPrimitive(it) }
            "multi" -> if (multiPick.isNotEmpty()) answers[current.id] = JsonArray(multiPick.map { JsonPrimitive(it) })
            "rating" -> if (rating > 0) answers[current.id] = JsonPrimitive(rating)
            else -> if (text.isNotBlank()) answers[current.id] = JsonPrimitive(text.trim())
        }
    }

    Card(
        modifier = modifier.fillMaxWidth().heightIn(min = 460.dp, max = 640.dp).popIn(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFE0E0E0))) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .fillMaxWidth(if (finished || questions.isEmpty()) 1f else (index + 1).toFloat() / questions.size)
                        .background(Primary)
                )
            }
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                if (!finished && q != null) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(campaign.title.uppercase(), color = Primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, maxLines = 1)
                            Text("Question ${index + 1} of ${questions.size}", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            "Skip", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { userSync.campaignEvent(campaign.id, "dismiss"); onSkip() }
                        )
                    }
                    if (index == 0 && !campaign.body.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(campaign.body, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(q.text, color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 26.sp)
                    Spacer(Modifier.height(18.dp))

                    Column(
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (q.type) {
                            "single", "multi", "poll" -> q.options.forEach { option ->
                                val selected = if (q.type != "multi") singlePick == option else option in multiPick
                                Card(
                                    onClick = {
                                        if (q.type != "multi") singlePick = option
                                        else multiPick = if (option in multiPick) multiPick - option else multiPick + option
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = if (selected) Primary else optionBg),
                                    border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                                ) {
                                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(22.dp)
                                                .clip(if (q.type != "multi") CircleShape else RoundedCornerShape(6.dp))
                                                .background(if (selected) Color.White.copy(alpha = 0.25f) else Color.White)
                                                .border(1.dp, if (selected) Color.White else Color.Gray.copy(alpha = 0.4f), if (q.type != "multi") CircleShape else RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Text(option, color = if (selected) Color.White else textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            "rating" -> Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                (1..5).forEach { n ->
                                    Box(
                                        modifier = Modifier.size(54.dp).clip(CircleShape)
                                            .background(if (rating == n) Primary else optionBg)
                                            .clickable { rating = n },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("$n", color = if (rating == n) Color.White else textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            else -> OutlinedTextField(
                                value = text,
                                onValueChange = { if (it.length <= 500) text = it },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                                shape = RoundedCornerShape(16.dp),
                                placeholder = { Text("Type your answer (optional)") }
                            )
                        }
                        error?.let { Text(it, color = Color(0xFFB71C1C), fontSize = 13.sp) }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            saveAnswer()
                            if (!last) {
                                index++
                            } else {
                                scope.launch {
                                    submitting = true
                                    val outcome = userSync.submitSurvey(campaign.id, answers.toMap())
                                    submitting = false
                                    if (outcome.problem == null) { pollResults = outcome.results; finished = true } else error = outcome.problem
                                }
                            }
                        },
                        enabled = canGoOn && !submitting,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, disabledContainerColor = Color(0xFFCCCCCC))
                    ) {
                        Text(
                            if (submitting) "Sending..." else if (last) (if (hasPoll) "Submit & see the results" else if (grantsPass) "Submit & claim 1-Hour Pass ⚡" else "Submit") else "Next question",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Celebration, null, tint = Primary, modifier = Modifier.size(38.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Thank you! 🎉", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (grantsPass) "Your answers were saved. You have unlocked 1 hour of reading without ads or surveys." else "Thank you, your answer was saved.",
                            color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        // how everyone voted in the poll questions
                        questions.filter { it.type == "poll" }.forEach { pq ->
                            val votes = pollResults[pq.id] ?: return@forEach
                            val total = votes.values.sum().coerceAtLeast(1)
                            val mine = (answers[pq.id] as? JsonPrimitive)?.content
                            Spacer(Modifier.height(18.dp))
                            Text(pq.text, color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            pq.options.forEachIndexed { i, option ->
                                PollBar(option, votes[option] ?: 0, total, option == mine, optionBg, textColor, i)
                                Spacer(Modifier.height(6.dp))
                            }
                            Text("$total ${if (total == 1) "vote" else "votes"}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(28.dp))
                        Button(
                            onClick = onComplete,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text(if (grantsPass) "Start 1-Hour Reading Pass ⚡" else "Back to the news", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


/** One option of a poll with its share of the votes; the bar grows in with a spring and the person's own pick is marked. */
@Composable
private fun PollBar(option: String, count: Int, total: Int, mine: Boolean, trackColor: Color, textColor: Color, index: Int) {
    val share = count.toFloat() / total
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(120L + index * 70L); shown = true }
    val fill by androidx.compose.animation.core.animateFloatAsState(
        if (shown) share else 0f,
        androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 120f),
        label = "pollBar"
    )
    Box(modifier = Modifier.fillMaxWidth().height(40.dp).clip(RoundedCornerShape(12.dp)).background(trackColor)) {
        Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fill.coerceIn(0f, 1f)).background(if (mine) Primary else Primary.copy(alpha = 0.35f)))
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (mine) "$option  ✓" else option, color = textColor, fontSize = 14.sp, fontWeight = if (mine) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.weight(1f), maxLines = 1)
            Text("${(share * 100).toInt()}%", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
