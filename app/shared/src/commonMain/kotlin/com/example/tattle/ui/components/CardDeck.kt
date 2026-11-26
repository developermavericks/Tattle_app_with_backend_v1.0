package com.example.tattle.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.tattle.models.Article

private class Ghost(val id: Int, val article: Article, val exit: ExitState, val bookmarked: Boolean)

/**
 * The stack of story cards. The top card is draggable; the next two are real cards waiting behind it. The moment a swipe counts, the next card
 * is already the top one and a copy of the swiped card flies away on its own, so nothing waits for an animation to finish.
 */
@Composable
fun CardDeck(
    articles: List<Article>,
    currentIndex: Int,
    bookmarks: List<String>,
    isDark: Boolean,
    onToggleBookmark: (Article) -> Unit,
    onShare: (Article) -> Unit,
    onOpenBrief: (Article) -> Unit,
    onOpenFull: (Article) -> Unit,
    onSwipe: (Article, SwipeDir) -> Unit,
    modifier: Modifier = Modifier
) {
    val ghosts = remember { mutableStateListOf<Ghost>() }
    var counter by remember { mutableIntStateOf(0) }

    BoxWithConstraints(modifier = modifier) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        val visible = (0..2).mapNotNull { depth -> articles.getOrNull(currentIndex + depth)?.let { it to depth } }

        for ((article, depth) in visible.reversed()) {                         // back to front
            key(article.id) {
                if (depth == 0) {
                    SwipeableArticleCard(
                        article = article,
                        isBookmarked = article.id in bookmarks,
                        onToggleBookmark = {
                            if (article.id in bookmarks) com.example.tattle.utils.Haptics.light() else com.example.tattle.utils.Haptics.success()
                            onToggleBookmark(article)
                        },
                        onShare = { com.example.tattle.utils.Haptics.light(); onShare(article) },
                        onOpenBrief = { onOpenBrief(article) },
                        onOpenFull = { onOpenFull(article) },
                        canGoBack = currentIndex > 0,
                        canAdvance = currentIndex < articles.size - 1,
                        onCommit = { dir, exit ->
                            com.example.tattle.utils.Haptics.light()                     // the card leaves the deck
                            ghosts.add(Ghost(counter++, article, exit, article.id in bookmarks))
                            onSwipe(article, dir)
                        },
                        isDark = isDark
                    )
                } else {
                    StackedCard(article = article, depth = depth, isDark = isDark)
                }
            }
        }

        for (ghost in ghosts.toList()) {                                       // flying away: drawn above everything
            key(ghost.id) {
                ExitingCard(
                    article = ghost.article,
                    isBookmarked = ghost.bookmarked,
                    isDark = isDark,
                    exit = ghost.exit,
                    widthPx = width,
                    heightPx = height,
                    onDone = { ghosts.remove(ghost) }
                )
            }
        }
    }
}
