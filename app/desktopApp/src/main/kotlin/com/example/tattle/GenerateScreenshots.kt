package com.example.tattle

import androidx.compose.runtime.Composable
import androidx.compose.ui.ImageComposeScene
import com.example.tattle.di.initKoin
import com.example.tattle.data.MockData
import com.example.tattle.models.*
import com.example.tattle.ui.screens.*
import com.example.tattle.ui.components.ShareCardDialog
import com.example.tattle.ui.theme.TattleTheme
import java.io.File
import kotlin.system.exitProcess

fun main() {
    println("=== Starting Screenshot Generation ===")
    try {
        initKoin()
    } catch (e: Exception) {
        println("Koin init notice: ${e.message}")
    }

    val targetDirs = listOf(
        File("C:/Users/MAVERICKS/AppData/Local/Google/AndroidStudio2026.1.3/projects/myapplication.14b6b93f/.artifacts/11031efc-4398-4f03-9894-b34dcff2844b"),
        File("F:/Tech-Folder/MyApplication/.artifacts/11031efc-4398-4f03-9894-b34dcff2844b")
    )

    for (dir in targetDirs) {
        dir.mkdirs()
    }

    val sampleArticle = MockData.articles.first()
    val prefs = UserPreferences(
        isOnboarded = true,
        ageGroup = "18-24",
        interests = listOf("ai", "tech", "startups"),
        language = "English",
        notifications = NotificationsPrefs(dailyBriefings = true, breakingAlerts = true, streaks = true),
        streak = 5,
        totalCardsRead = 42,
        readHistory = listOf("1"),
        bookmarks = listOf("1"),
        reactions = mapOf("1" to "like"),
        adFreeUntil = null,
        sensitivity = "medium",
        lastReadDate = "2026-09-16",
        isDarkMode = false
    )

    fun renderAndSave(fileName: String, content: @Composable () -> Unit) {
        val scene = ImageComposeScene(420, 880)
        scene.setContent {
            TattleTheme {
                content()
            }
        }
        val skiaImage = scene.render()
        val bytes = skiaImage.encodeToData()?.bytes
        if (bytes != null) {
            for (dir in targetDirs) {
                val destFile = File(dir, fileName)
                destFile.writeBytes(bytes)
                println("Saved ${destFile.absolutePath} successfully (${bytes.size} bytes)")
            }
        } else {
            println("Failed to render $fileName")
        }
        scene.close()
    }

    // 1. Splash Screen
    renderAndSave("splash_screen.png") {
        SplashScreen(isDark = false, onGetStarted = {})
    }

    // 2. Onboarding Screen
    renderAndSave("onboarding_screen.png") {
        OnboardingScreen(onComplete = {}, onBack = {})
    }

    // 3. Feed Screen
    renderAndSave("feed_screen.png") {
        FeedScreen(
            preferences = prefs,
            activeTab = "ai",
            onTabSelected = {},
            onUpdatePreferences = {},
            onOpenArticle = {},
            onLaunchBrief = {},
            onShareArticle = {},
            onOpenSettings = {},
            onSessionEnd = {}
        )
    }

    // 4. Explore Screen
    renderAndSave("explore_screen.png") {
        ExploreScreen(
            preferences = prefs,
            onCategoryClick = {},
            onOpenArticle = {},
            onOpenSettings = {}
        )
    }

    // 5. Trending Screen
    renderAndSave("trending_screen.png") {
        TrendingScreen(
            preferences = prefs,
            onOpenArticle = {},
            onOpenSettings = {}
        )
    }

    // 6. Saved Screen
    renderAndSave("saved_screen.png") {
        SavedScreen(
            preferences = prefs,
            onOpenArticle = {},
            onOpenSettings = {}
        )
    }

    // 7. Recap Screen
    renderAndSave("recap_screen.png") {
        RecapScreen(
            preferences = prefs,
            onTuneFeed = {},
            onBack = {}
        )
    }

    // 8. Settings Screen
    renderAndSave("settings_screen.png") {
        SettingsScreen(
            preferences = prefs,
            onUpdatePreferences = {},
            onOpenArticle = {},
            onPurgeData = {},
            onBack = {}
        )
    }

    // 9. Brief View Overlay
    renderAndSave("brief_view.png") {
        BriefView(
            article = sampleArticle,
            isBookmarked = true,
            onClose = {},
            onToggleBookmark = {},
            onShare = {},
            onLaunchFullText = {},
            isDark = false
        )
    }

    // 10. Full View Overlay
    renderAndSave("full_view.png") {
        FullView(
            article = sampleArticle,
            isBookmarked = true,
            onClose = {},
            onToggleBookmark = {},
            onShare = {},
            isDark = false
        )
    }

    // 11. Share Card Dialog
    renderAndSave("share_dialog.png") {
        ShareCardDialog(
            article = sampleArticle,
            onDismiss = {},
            onConfirmShare = {}
        )
    }

    println("=== All Screenshots Generated Successfully ===")
    exitProcess(0)
}
