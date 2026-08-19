package com.example.tattle

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tattle.data.MockData
import com.example.tattle.models.Article
import com.example.tattle.ui.screens.*
import com.example.tattle.ui.theme.LocalAppLanguage
import com.example.tattle.ui.theme.LocalStrings
import com.example.tattle.ui.theme.TattleTheme
import com.example.tattle.ui.viewmodels.AppViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Feed : Screen("feed")
    data object Recap : Screen("recap")
    data object Settings : Screen("settings")
}

@Composable
fun App() {
    KoinContext {
        val viewModel: AppViewModel = koinViewModel()
        val preferences by viewModel.preferences.collectAsState()
        val navController = rememberNavController()
        val appLanguage = preferences?.language ?: "English"

        CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
            TattleTheme {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Overlays
                var activeArticle by remember { mutableStateOf<Article?>(null) }
                var activeBriefArticle by remember { mutableStateOf<Article?>(null) }

                // Auto-redirect from Splash if already onboarded
                LaunchedEffect(preferences?.isOnboarded) {
                    if (preferences?.isOnboarded == true && currentRoute == Screen.Splash.route) {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }

                Scaffold(
                    bottomBar = {
                        if (currentRoute in listOf(Screen.Feed.route, Screen.Recap.route, Screen.Settings.route)) {
                            BottomNav(
                                currentRoute = currentRoute ?: "",
                                onScreenSelected = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Feed.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onExploreClick = {
                                    activeBriefArticle = MockData.articles.random()
                                }
                            )
                        }
                    },
                    containerColor = com.example.tattle.ui.theme.Background
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route,
                            enterTransition = { fadeIn(tween(500)) },
                            exitTransition = { fadeOut(tween(500)) }
                        ) {
                            composable(Screen.Splash.route) {
                                SplashScreen(onGetStarted = {
                                    // Priority Check: If interests are empty, they ARE NOT onboarded correctly.
                                    val isCorrectlyOnboarded = preferences?.let { 
                                        it.isOnboarded && it.interests.isNotEmpty() 
                                    } ?: false
                                    
                                    val nextRoute = if (isCorrectlyOnboarded) Screen.Feed.route else Screen.Onboarding.route
                                    
                                    navController.navigate(nextRoute) {
                                        popUpTo(Screen.Splash.route) { inclusive = true }
                                    }
                                })
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
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onOpenArticle = { activeArticle = it },
                                        onLaunchBrief = { activeBriefArticle = it },
                                        onShareArticle = { /* Share simulation */ }
                                    )
                                }
                            }
                            composable(Screen.Recap.route) {
                                preferences?.let { prefs ->
                                    RecapScreen(
                                        preferences = prefs,
                                        onTuneFeed = { navController.navigate(Screen.Settings.route) }
                                    )
                                }
                            }
                            composable(Screen.Settings.route) {
                                preferences?.let { prefs ->
                                    SettingsScreen(
                                        preferences = prefs,
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onPurgeData = {
                                            viewModel.purgeData()
                                            navController.navigate(Screen.Splash.route) {
                                                popUpTo(0) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Overlays
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
                                    onShare = { /* Share */ },
                                    onLaunchFullText = {
                                        activeArticle = article
                                        activeBriefArticle = null
                                    }
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
                                    onShare = { /* Share */ },
                                    nextArticle = MockData.articles.getOrNull((MockData.articles.indexOf(article) + 1) % MockData.articles.size),
                                    onLoadNextArticle = { activeArticle = it }
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
    onScreenSelected: (Screen) -> Unit,
    onExploreClick: () -> Unit,
) {
    val language = LocalAppLanguage.current
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(32.dp))
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.Feed.route,
            onClick = { onScreenSelected(Screen.Feed) },
            icon = { Icon(Icons.Default.Bolt, null) },
            label = { Text(LocalStrings.get("for_you", language), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.tattle.ui.theme.Primary,
                selectedTextColor = com.example.tattle.ui.theme.Primary,
                unselectedIconColor = com.example.tattle.ui.theme.TextPrimary,
                unselectedTextColor = com.example.tattle.ui.theme.TextPrimary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onExploreClick,
            icon = { Icon(Icons.Default.Explore, null) },
            label = { Text(LocalStrings.get("explore", language), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = com.example.tattle.ui.theme.TextPrimary,
                unselectedTextColor = com.example.tattle.ui.theme.TextPrimary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Recap.route,
            onClick = { onScreenSelected(Screen.Recap) },
            icon = { Icon(Icons.Default.BarChart, null) },
            label = { Text(LocalStrings.get("trending", language), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.tattle.ui.theme.Primary,
                selectedTextColor = com.example.tattle.ui.theme.Primary,
                unselectedIconColor = com.example.tattle.ui.theme.TextPrimary,
                unselectedTextColor = com.example.tattle.ui.theme.TextPrimary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = { onScreenSelected(Screen.Settings) },
            icon = { Icon(Icons.Default.Favorite, null) },
            label = { Text(LocalStrings.get("saved", language), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.tattle.ui.theme.Primary,
                selectedTextColor = com.example.tattle.ui.theme.Primary,
                unselectedIconColor = com.example.tattle.ui.theme.TextPrimary,
                unselectedTextColor = com.example.tattle.ui.theme.TextPrimary,
                indicatorColor = Color.Transparent
            )
        )
    }
}
