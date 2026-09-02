package com.example.tattle

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.example.tattle.models.Article
import com.example.tattle.ui.screens.*
import com.example.tattle.ui.theme.LocalAppLanguage
import com.example.tattle.ui.theme.LocalStrings
import com.example.tattle.ui.theme.TattleTheme
import com.example.tattle.ui.viewmodels.AppViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

import com.example.tattle.utils.shareArticleContent

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
        val sessionStatus by viewModel.sessionStatus.collectAsState()
        val currentFeedTab by viewModel.currentFeedTab.collectAsState()
        val navController = rememberNavController()
        val appLanguage = preferences?.language ?: "English"

        CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
            TattleTheme {
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStack?.destination?.route

                // Overlays
                var activeArticle by remember { mutableStateOf<Article?>(null) }
                var activeBriefArticle by remember { mutableStateOf<Article?>(null) }

                // Auto-redirect from Splash if already onboarded or authenticated
                LaunchedEffect(preferences?.isOnboarded, sessionStatus) {
                    if (currentRoute == Screen.Splash.route) {
                        val isCorrectlyOnboarded = preferences?.let { 
                            it.isOnboarded && it.interests.isNotEmpty() 
                        } ?: false
                        
                        if (isCorrectlyOnboarded || sessionStatus is SessionStatus.Authenticated) {
                            val nextRoute = if (isCorrectlyOnboarded) Screen.Feed.route else Screen.Onboarding.route
                            navController.navigate(nextRoute) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
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
                                currentRoute = currentRoute ?: "",
                                onScreenSelected = { screen ->
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
                    containerColor = com.example.tattle.ui.theme.Background
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route,
                            enterTransition = { 
                                if (initialState.destination.route == Screen.Splash.route) {
                                    fadeIn(tween(500))
                                } else {
                                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) + fadeIn()
                                }
                            },
                            exitTransition = { 
                                if (targetState.destination.route == Screen.Splash.route) {
                                    fadeOut(tween(500))
                                } else {
                                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) + fadeOut()
                                }
                            }
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
                                        activeTab = currentFeedTab ?: prefs.interests.firstOrNull() ?: "for_you",
                                        onTabSelected = { viewModel.setFeedTab(it) },
                                        onUpdatePreferences = { viewModel.updatePreferences(it) },
                                        onOpenArticle = { activeArticle = it },
                                        onLaunchBrief = { activeBriefArticle = it },
                                        onShareArticle = { article -> shareArticleContent(article) },
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
                                        onOpenSettings = { navController.navigate(Screen.Settings.route) }
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
                                    onShare = { shareArticleContent(article) },
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
                                    onShare = { shareArticleContent(article) }
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
            selected = currentRoute == Screen.Explore.route,
            onClick = { onScreenSelected(Screen.Explore) },
            icon = { Icon(Icons.Default.Explore, null) },
            label = { Text(LocalStrings.get("explore", language), fontSize = 12.sp, fontWeight = FontWeight.Medium) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = com.example.tattle.ui.theme.Primary,
                selectedTextColor = com.example.tattle.ui.theme.Primary,
                unselectedIconColor = com.example.tattle.ui.theme.TextPrimary,
                unselectedTextColor = com.example.tattle.ui.theme.TextPrimary,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.Trending.route,
            onClick = { onScreenSelected(Screen.Trending) },
            icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) },
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
            selected = currentRoute == Screen.Saved.route,
            onClick = { onScreenSelected(Screen.Saved) },
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
