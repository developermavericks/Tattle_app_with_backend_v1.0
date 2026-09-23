package com.example.tattle

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

        val isAlreadyLoggedIn = preferences?.isOnboarded == true || preferences?.phoneNumber?.isNotBlank() == true || preferences?.email?.isNotBlank() == true
        val initialDestination = if (isAlreadyLoggedIn) Screen.Feed.route else Screen.Splash.route
        val navController = rememberNavController()
        val kamelConfig: KamelConfig = koinInject()
        val imageRepository: ImageRepository = koinInject()

        // App-wide Image Warmup: Pre-fetches & decodes ImageBitmaps into RAM as soon as app launches
        LaunchedEffect(Unit) {
            val warmupList = MockData.articles.take(20) + MockData.getTrendingArticles()
            warmupList.forEach { article ->
                launch(Dispatchers.Default) {
                    imageRepository.getOrFetchBitmapForArticle(article)
                }
            }
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
                                if (initialState.destination.route == Screen.Splash.route) {
                                    fadeIn(tween(300))
                                } else {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()
                                }
                            },
                            exitTransition = { 
                                if (targetState.destination.route == Screen.Splash.route) {
                                    fadeOut(tween(300))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
                                }
                            }
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen(
                                    isDark = isDark,
                                    onGetStarted = {
                                        val hasCompletedOnboarding = preferences?.isOnboarded == true && preferences?.ageGroup?.isNotBlank() == true
                                        val nextRoute = if (hasCompletedOnboarding) Screen.Feed.route else Screen.Onboarding.route
                                        navController.navigate(nextRoute) {
                                            popUpTo(Screen.Splash.route) { inclusive = true }
                                        }
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
                            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
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
                            enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
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
    val unselectedColor = if (isDark) DarkTextMuted else TextMuted
    NavigationBar(
        containerColor = if (isDark) DarkSurface else Color.White,
        contentColor = Primary,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Feed.route,
            onClick = { onScreenSelected(Screen.Feed) },
            icon = { Icon(Icons.Default.Bolt, contentDescription = LocalStrings.get("for_you", language)) },
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
            onClick = { onScreenSelected(Screen.Explore) },
            icon = { Icon(Icons.Default.Explore, contentDescription = LocalStrings.get("explore", language)) },
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
            onClick = { onScreenSelected(Screen.Trending) },
            icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = LocalStrings.get("trending", language)) },
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
            onClick = { onScreenSelected(Screen.Saved) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = LocalStrings.get("saved", language)) },
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
