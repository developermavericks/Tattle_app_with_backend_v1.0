package com.example.tattle.models

enum class TourStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val feedback: String,
    val totalSteps: Int = 10
) {
    NONE(
        stepNumber = 0,
        title = "",
        instruction = "",
        feedback = ""
    ),
    SWIPE_RIGHT(
        stepNumber = 1,
        title = "Mark as Relevant",
        instruction = "Swipe Right on the card to mark it relevant and save it",
        feedback = "Awesome! Marked as Relevant. We'll show you more similar stories 🟢"
    ),
    SWIPE_LEFT(
        stepNumber = 2,
        title = "Mark as Not Relevant",
        instruction = "Swipe Left on the card if you find it not relevant",
        feedback = "Got it! Marked as Not Relevant to tune your feed 🔴"
    ),
    SWIPE_UP(
        stepNumber = 3,
        title = "Quick Skip",
        instruction = "Swipe Up on the card to skip it without rating",
        feedback = "Skipped! You can skip cards anytime without rating them ⚡"
    ),
    TAP_BRIEF(
        stepNumber = 4,
        title = "Open Brief View",
        instruction = "Tap the card to open its quick summary & key takeaways",
        feedback = "Opened Brief View! Great for 30-second bullet summaries 🎯"
    ),
    FULL_ARTICLE(
        stepNumber = 5,
        title = "Full Depth Report",
        instruction = "Scroll down & tap 'Launch Full Report' for deep reading",
        feedback = "Opened Full Report! Read the full story narrative in depth 📖"
    ),
    SHARE_ARTICLE(
        stepNumber = 6,
        title = "Share Stories",
        instruction = "Tap the Share button to send interesting stories to friends",
        feedback = "Opened Share dialog! Share stories easily across any platform 📤"
    ),
    EXPLORE_TAB(
        stepNumber = 7,
        title = "Explore Categories",
        instruction = "Tap 'Explore' on the bottom bar to browse topics & sectors",
        feedback = "Switched to Explore! Browse 15+ news categories & topics 🔍"
    ),
    DAILY_RECAP(
        stepNumber = 8,
        title = "Daily Reading Recap",
        instruction = "Tap 'Recap' at the top of the feed to view your reading streak & stats",
        feedback = "Opened Daily Recap! Track your cards read, streak & breakdown 📊"
    ),
    SETTINGS_DARK_MODE(
        stepNumber = 9,
        title = "Theme & Settings",
        instruction = "Tap 'Settings' in the top bar to toggle Dark Mode & preferences",
        feedback = "Opened Settings! Toggle Dark Mode or customize your experience ⚙️"
    ),
    TRENDING_SWIPE(
        stepNumber = 10,
        title = "Trending Story Switcher",
        instruction = "On Trending, open any story & swipe Left/Right to switch trending stories!",
        feedback = "Congratulations! You've mastered all features of Tattle! 🎉"
    );

    fun next(): TourStep {
        return when (this) {
            SWIPE_RIGHT -> SWIPE_LEFT
            SWIPE_LEFT -> SWIPE_UP
            SWIPE_UP -> TAP_BRIEF
            TAP_BRIEF -> FULL_ARTICLE
            FULL_ARTICLE -> SHARE_ARTICLE
            SHARE_ARTICLE -> EXPLORE_TAB
            EXPLORE_TAB -> DAILY_RECAP
            DAILY_RECAP -> SETTINGS_DARK_MODE
            SETTINGS_DARK_MODE -> TRENDING_SWIPE
            TRENDING_SWIPE -> NONE
            NONE -> NONE
        }
    }
}
