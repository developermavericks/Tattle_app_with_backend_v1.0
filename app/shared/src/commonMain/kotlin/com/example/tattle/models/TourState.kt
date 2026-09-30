package com.example.tattle.models

enum class TourStep(
    val stepNumber: Int,
    val totalSteps: Int,
    val title: String,
    val instruction: String,
    val feedback: String
) {
    NONE(0, 7, "", "", ""),

    SWIPE_RIGHT(
        1, 7,
        "Swipe Right to Read & Save ➡️",
        "Swipe the card to the RIGHT to mark it as read and save it to your reading history.",
        "Great job! Card saved to history 🟢"
    ),

    SWIPE_LEFT(
        2, 7,
        "Swipe Left to Skip ⬅️",
        "Swipe the card to the LEFT to pass without recording history.",
        "Passed! Moving to next story ⏩"
    ),

    SWIPE_UP(
        3, 7,
        "Swipe Up for Fast Skip ⬆️",
        "Swipe the card UPWARDS off the top of the screen to quickly skip.",
        "Fast skipped! ⚡"
    ),

    SWIPE_DOWN(
        4, 7,
        "Swipe Down to Bring Back ⬇️",
        "Swipe DOWNWARDS from top to bottom to bring back the previous card.",
        "Brought back previous story! ⏪"
    ),

    TAP_BRIEF(
        5, 7,
        "Tap Card for Executive Brief 👆",
        "TAP anywhere on the card to open the 30-second AI Executive Brief.",
        "Opened Executive Brief! 📰"
    ),

    FULL_ARTICLE(
        6, 7,
        "Launch Full In-Depth Report 📖",
        "Tap 'Launch Full Depth Report' at the bottom of the brief to read the full narrative.",
        "Opened Full In-Depth Report! 📚"
    ),

    TRENDING_SWIPE(
        7, 7,
        "Swipe Brief Right-to-Left ↔️",
        "Inside Brief View, swipe horizontally right-to-left to switch to the next story instantly.",
        "Switched to next brief! Tour complete! 🎉"
    );

    fun next(): TourStep {
        return when (this) {
            SWIPE_RIGHT -> SWIPE_LEFT
            SWIPE_LEFT -> SWIPE_UP
            SWIPE_UP -> SWIPE_DOWN
            SWIPE_DOWN -> TAP_BRIEF
            TAP_BRIEF -> FULL_ARTICLE
            FULL_ARTICLE -> TRENDING_SWIPE
            TRENDING_SWIPE -> NONE
            NONE -> NONE
        }
    }
}
