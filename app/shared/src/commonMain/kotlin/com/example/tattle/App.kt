package com.example.tattle

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tattle.data.ImageRepository
import com.example.tattle.data.MockData
import com.example.tattle.models.Article
import com.example.tattle.ui.components.ShareCardDialog
import com.example.tattle.ui.screens.*
import com.example.tattle.ui.theme.*
import com.example.tattle.ui.viewmodels.AppViewModel
import io.kamel.core.config.KamelConfig
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Username : Screen("username")
    data object Feed : Screen("feed")
    data object Explore : Screen("explore")
    data object Trending : Screen("trending")
    data object Saved : Screen("saved")
    data object Recap : Screen("recap")
    data object Settings : Screen("settings")
}

@Composable
fun App() {
    KoinContext {
        val viewModel: AppViewModel = koinViewModel()
        val preferences by viewModel.preferences.collectAsState()
        val currentFeedTab by viewModel.currentFeedTab.collectAsState()
        val appLanguage = preferences?.language ?: "English"
        val isDark = preferences?.isDarkMode == true

        if (preferences == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isDark) DarkBackground else Background)
            )
            return@KoinContext
        }

        val isAlreadyLoggedIn = preferences?.isOnboarded == true && preferences?.dob?.isNotBlank() == true
        val initialDestination = if (isAlreadyLoggedIn) Screen.Feed.route else Screen.Splash.route
        val navController = rememberNavController()
        val kamelConfig: KamelConfig = koinInject()
        val userSync: com.example.tattle.data.UserSync = koinInject()
        val appScope = rememberCoroutineScope()

        // People who joined before usernames existed are asked for one, once
        val needsUsername by userSync.needsUsername.collectAsState()
        LaunchedEffect(needsUsername) {
            if (needsUsername) navController.navigate(Screen.Username.route) { popUpTo(Screen.Feed.route) { inclusive = true } }
        }

        // The account was deleted on the server: back to login and onboarding
        LaunchedEffect(Unit) {
            viewModel.sessionEnded.collect {
                navController.navigate(Screen.Splash.route) { popUpTo(0) { inclusive = true } }
            }
        }
        val imageRepository: ImageRepository = koinInject()
        val articleRepository: com.example.tattle.data.ArticleRepository = koinInject()

        // App-wide Image Warmup: Pre-fetches & decodes ImageBitmaps into RAM as soon as app launches
        LaunchedEffect(Unit) {
            // (the feed pre-fetches the pictures of its own, real stories)
        }

        CompositionLocalProvider(
            LocalAppLanguage provides appLanguage,
            LocalKamelConfig provides kamelConfig
        ) {
            TattleTheme(darkTheme = isDark) {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route ?: initialDestination

                // Overlays
                var activeArticle by remember { mutableStateOf<Article?>(null) }
                var activeBriefArticle by remember { mutableStateOf<Article?>(null) }
                var shareDialogArticle by remember { mutableStateOf<Article?>(null) }

                // Opened from a breaking-news notification (MainActivity set this on tap): fetch that one story and
                // show it directly. Only once logged in/onboarded - otherwise left pending until the person gets there,
                // since the key below includes isAlreadyLoggedIn and the effect re-runs when it changes.
                val pendingArticleId = com.example.tattle.utils.PendingDeepLink.articleId
                LaunchedEffect(pendingArticleId, isAlreadyLoggedIn) {
                    val targetId = pendingArticleId ?: return@LaunchedEffect
                    if (!isAlreadyLoggedIn) return@LaunchedEffect
                    val fetched = runCatching { articleRepository.getArticlesByIdsFromBackend(listOf(targetId)) }.getOrNull()
                    fetched?.firstOrNull()?.let { activeArticle = it }
                    com.example.tattle.utils.PendingDeepLink.articleId = null
                }

                // what the person was doing, attached to a crash report so it can be reproduced
                LaunchedEffect(currentRoute) {
                    com.example.tattle.utils.Breadcrumbs.screen = currentRoute
                    com.example.tattle.utils.Breadcrumbs.add("screen $currentRoute")
                }

                // Asked once, after onboarding, in plain words. Nothing is read from the phone before the person says yes.
                var askLocation by remember { mutableStateOf(false) }
                LaunchedEffect(currentRoute) {
                    if (currentRoute == Screen.Feed.route && userSync.locationChoice == null) askLocation = true
                }
                if (askLocation) {
                    AlertDialog(
                        onDismissRequest = { userSync.locationChoice = "no"; askLocation = false },
                        title = { Text("Stories near you?") },
                        text = {
                            Text("Allow Tattle to use your location to show stories from your city and nearby? " +
                                "You can turn this off any time in Settings.")
                        },
                        confirmButton = {
                            TextButton(onClick = { askLocation = false; appScope.launch { userSync.enableLocation() } }) { Text("Allow") }
                        },
                        dismissButton = {
                            TextButton(onClick = { userSync.locationChoice = "no"; askLocation = false }) { Text("Not now") }
                        }
                    )
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute in listOf(
                                Screen.Feed.route,
                                Screen.Explore.route,
                                Screen.Trending.route,
                                Screen.Saved.route,
                                Screen.Recap.route,
                                Screen.Settings.route
                            )
                        ) {
                            BottomNav(
                                currentRoute = currentRoute,
                                isDark = isDark,
                                onScreenSelected = { screen ->
                                    // Smoothly dismiss any open reader overlay when tapping bottom nav tabs
                                    activeArticle = null
                                    activeBriefArticle = null
                                    shareDialogArticle = null

                                    // Always return to the main stream here, even when a sector picked from Explore left the feed already on this route
                                    // (the route-equality check below would otherwise skip the whole block and leave that sector's stream showing).
                                    if (screen == Screen.Feed) viewModel.setFeedTab("for_you")

                                    if (currentRoute != screen.route) {
                                        if (screen == Screen.Feed) {
                                            navController.popBackStack(Screen.Feed.route, inclusive = false)
                                        } else {
                                            navController.navigate(screen.route) {
                                                popUpTo(Screen.Feed.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    },
                    containerColor = if (isDark) DarkBackground else Background
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        NavHost(
                            navController = navController,
                            startDestination = initialDestination,
                            enterTransition = {
                                if (targetState.destination.route in listOf(Screen.Settings.route, Screen.Recap.route))
                                    slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 380f)) { it / 3 } + fadeIn(tween(200))       // pushed like on an iPhone
                                else fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing))
                            },
                            exitTransition = {
                                if (targetState.destination.route in listOf(Screen.Settings.route, Screen.Recap.route))
                                    slideOutHorizontally(tween(260, easing = FastOutSlowInEasing)) { -it / 5 } + fadeOut(tween(200))           // the page underneath moves a little
                                else fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing))
                            },
                            popEnterTransition = { slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 380f)) { -it / 5 } + fadeIn(tween(200)) },
                            popExitTransition = { slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { it / 3 } + fadeOut(tween(180)) }
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen(
                                    isDark = isDark,
                                    onGetStarted = { needsOnboarding ->
                                        val nextRoute = if (needsOnboarding) Screen.Onboarding.route else Screen.Feed.route
                                        navController.navigate(nextRoute) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Username.route) {
                                com.example.tattle.ui.screens.UsernameGateScreen(
                                    initialName = preferences?.name ?: "",
                                    onDone = { name, username ->
                                        preferences?.let { viewModel.updatePreferences(it.copy(name = name, username = username)) }
                                        userSync.usernameDone()
                                        navController.navigate(Screen.Feed.route) { popUpTo(Screen.Username.route) { inclusive = true } }
                                    }
                                )
                            }
                            composable(Screen.Onboarding.route) {
                                OnboardingScreen(
                                    onComplete = {
                                        viewModel.updatePreferences(it)
                                        navController.navigate(Screen.Feed.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    },
                                    onBack = {
                                        navController.navigate(Screen.Splash.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    }
                                )
                            }
                            composable(Screen.Feed.route) {
                                preferences?.let { prefs ->
                                    FeedScreen(
                                        preferences = prefs,
                                        activeTab = currentFeedTab ?: prefs.interests.firstOrNull() ?: "for_you",
                                        onTabSelected = { viewModel.setFeedTab(it) },
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onOpenArticle = { activeArticle = it },
                                        onLaunchBrief = { activeBriefArticle = it },
                                        onShareArticle = { article -> shareDialogArticle = article },
                                        onOpenSettings = { navController.navigate(Screen.Settings.route) },
                                        onSessionEnd = { navController.navigate(Screen.Recap.route) }
                                    )
                                }
                            }
                            composable(Screen.Explore.route) {
                                preferences?.let { prefs ->
                                    ExploreScreen(
                                        preferences = prefs,
                                        onCategoryClick = { sector ->
                                            viewModel.setFeedTab(sector)
                                            navController.navigate(Screen.Feed.route) {
                                                popUpTo(Screen.Feed.route) { inclusive = true }
                                            }
                                        },
                                        onOpenArticle = { activeArticle = it },
                                        onOpenSettings = { navController.navigate(Screen.Settings.route) }
                                    )
                                }
                            }
                            composable(Screen.Trending.route) {
                                preferences?.let { prefs ->
                                    TrendingScreen(
                                        preferences = prefs,
                                        onOpenArticle = { activeArticle = it },
                                        onOpenSettings = { navController.navigate(Screen.Settings.route) },
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onShareArticle = { article -> shareDialogArticle = article }
                                    )
                                }
                            }
                            composable(Screen.Saved.route) {
                                preferences?.let { prefs ->
                                    SavedScreen(
                                        preferences = prefs,
                                        onOpenArticle = { activeArticle = it },
                                        onOpenSettings = { navController.navigate(Screen.Settings.route) }
                                    )
                                }
                            }
                            composable(Screen.Recap.route) {
                                preferences?.let { prefs ->
                                    RecapScreen(
                                        preferences = prefs,
                                        onTuneFeed = { navController.navigate(Screen.Settings.route) },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                            composable(Screen.Settings.route) {
                                preferences?.let { prefs ->
                                    SettingsScreen(
                                        preferences = prefs,
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onOpenArticle = { activeArticle = it },
                                        onPurgeData = {
                                            viewModel.purgeData()
                                            navController.navigate(Screen.Splash.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        },
                                        onBack = {
                                            navController.popBackStack()
                                        }
                                    )
                                }
                            }
                        }

                        // Overlays
                        if (shareDialogArticle != null) {
                            ShareCardDialog(
                                article = shareDialogArticle!!,
                                onDismiss = { shareDialogArticle = null }
                            )
                        }

                        AnimatedVisibility(
                            visible = activeBriefArticle != null,
                            enter = slideInVertically(spring(dampingRatio = 0.86f, stiffness = 420f)) { it / 3 } + fadeIn(tween(160)) +
                                scaleIn(spring(dampingRatio = 0.86f, stiffness = 420f), initialScale = 0.94f),
                            exit = slideOutVertically(tween(200, easing = FastOutLinearInEasing)) { it / 3 } + fadeOut(tween(160)) + scaleOut(tween(200), targetScale = 0.96f)
                        ) {
                            activeBriefArticle?.let { article ->
                                BriefView(
                                    article = article,
                                    isBookmarked = preferences?.bookmarks?.contains(article.id) == true,
                                    onClose = { activeBriefArticle = null },
                                    onToggleBookmark = {
                                        preferences?.let { prefs ->
                                            val newBookmarks = if (prefs.bookmarks.contains(article.id)) {
                                                prefs.bookmarks - article.id
                                            } else {
                                                prefs.bookmarks + article.id
                                            }
                                            viewModel.updatePreferences(prefs.copy(bookmarks = newBookmarks))
                                        }
                                    },
                                    onShare = { shareDialogArticle = article },
                                    onLaunchFullText = {
                                        activeArticle = article
                                        activeBriefArticle = null
                                    },
                                    isDark = preferences?.isDarkMode == true
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = activeArticle != null,
                            enter = slideInHorizontally(spring(dampingRatio = 0.9f, stiffness = 380f)) { it / 2 } + fadeIn(tween(160)),
                            exit = slideOutHorizontally(tween(210, easing = FastOutLinearInEasing)) { it / 2 } + fadeOut(tween(160))
                        ) {
                            activeArticle?.let { article ->
                                FullView(
                                    article = article,
                                    isBookmarked = preferences?.bookmarks?.contains(article.id) == true,
                                    onClose = { activeArticle = null },
                                    onToggleBookmark = {
                                        preferences?.let { prefs ->
                                            val newBookmarks = if (prefs.bookmarks.contains(article.id)) {
                                                prefs.bookmarks - article.id
                                            } else {
                                                prefs.bookmarks + article.id
                                            }
                                            viewModel.updatePreferences(prefs.copy(bookmarks = newBookmarks))
                                        }
                                    },
                                    onShare = { shareDialogArticle = article },
                                    isDark = preferences?.isDarkMode == true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNav(
    currentRoute: String,
    isDark: Boolean,
    onScreenSelected: (Screen) -> Unit,
) {
    val language = LocalAppLanguage.current
    val select: (Screen) -> Unit = {
        com.example.tattle.utils.Haptics.selection()
        onScreenSelected(it)
    }
    val unselectedColor = if (isDark) DarkTextMuted else TextMuted
    NavigationBar(
        containerColor = if (isDark) DarkSurface else Color.White,
        contentColor = Primary,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Feed.route,
            onClick = { select(Screen.Feed) },
            icon = { NavIcon(selected = currentRoute == Screen.Feed.route) { Icon(Icons.Default.Bolt, contentDescription = LocalStrings.get("for_you", language)) } },
            label = { Text(LocalStrings.get("for_you", language), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.1f),
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Explore.route,
            onClick = { select(Screen.Explore) },
            icon = { NavIcon(selected = currentRoute == Screen.Explore.route) { Icon(Icons.Default.Explore, contentDescription = LocalStrings.get("explore", language)) } },
            label = { Text(LocalStrings.get("explore", language), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.1f),
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Trending.route,
            onClick = { select(Screen.Trending) },
            icon = { NavIcon(selected = currentRoute == Screen.Trending.route) { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = LocalStrings.get("trending", language)) } },
            label = { Text(LocalStrings.get("trending", language), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.1f),
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Saved.route,
            onClick = { select(Screen.Saved) },
            icon = { NavIcon(selected = currentRoute == Screen.Saved.route) { Icon(Icons.Default.Bookmark, contentDescription = LocalStrings.get("saved", language)) } },
            label = { Text(LocalStrings.get("saved", language), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Primary,
                selectedTextColor = Primary,
                indicatorColor = Primary.copy(alpha = 0.1f),
                unselectedIconColor = unselectedColor,
                unselectedTextColor = unselectedColor
            )
        )
    }
}


/** A tab icon that bounces when it becomes the selected one. */
@Composable
private fun NavIcon(selected: Boolean, content: @Composable () -> Unit) {
    val scale by animateFloatAsState(if (selected) 1.15f else 1f, spring(dampingRatio = 0.4f, stiffness = 500f), label = "navIcon")
    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) { content() }
}
