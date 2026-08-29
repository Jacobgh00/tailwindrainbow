package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

/**
 * Answers whether a string reads as a Tailwind class list on its own, for the strings no syntax rule
 * claimed. Every word has to be one, because a known variant in front of any tail is a shape prose
 * reaches by accident — `Verhalten: hover:aktiv` — and a whole string of them is not.
 */
internal class ClassListShape(private val themeMatcher: ThemeMatcher) {
    fun readsAsClassList(content: String): Boolean {
        if (content.length > MAX_LENGTH || ':' !in content) {
            return false
        }

        val words = content.classWords()
        if (words.isEmpty() || words.size > MAX_WORDS) {
            return false
        }

        var variants = 0

        for (word in words) {
            when (shapeOf(word.value)) {
                WordShape.FOREIGN -> return false
                WordShape.VARIANT -> variants++
                WordShape.UTILITY -> Unit
            }
        }

        return variants > 0
    }

    private fun shapeOf(word: String): WordShape {
        if (word.startsWith(':') || word.endsWith(':')) {
            return WordShape.FOREIGN
        }

        val parts = word.splitOnUnnestedColons(0).map(ClassPart::value)
        if (parts.any(String::isEmpty)) {
            return WordShape.FOREIGN
        }

        if (!parts.last().looksLikeUtility()) {
            return WordShape.FOREIGN
        }

        if (parts.size == 1) {
            return WordShape.UTILITY
        }

        val named = parts.dropLast(1).any { themeMatcher.matchPrefix(it) != null }

        return if (named) WordShape.VARIANT else WordShape.FOREIGN
    }

    private companion object {
        const val MAX_LENGTH = 512
        const val MAX_WORDS = 64
    }
}

private enum class WordShape { FOREIGN, VARIANT, UTILITY }

private fun String.looksLikeUtility(): Boolean {
    val bare = trim('!')

    return bare.isNotEmpty() &&
        (bare.startsWith('[') || bare.any { it == '-' || it == '/' || it.isDigit() } || bare in BARE_UTILITIES)
}

private val BARE_UTILITIES =
    setOf(
        "flex", "grid", "block", "inline", "hidden", "contents", "table", "flow-root",
        "static", "fixed", "absolute", "relative", "sticky",
        "visible", "invisible", "collapse", "isolate", "container",
        "underline", "overline", "italic", "truncate", "uppercase", "lowercase", "capitalize",
        "antialiased", "border", "rounded", "shadow", "ring", "outline", "transition", "transform",
        "filter", "resize", "group", "peer", "dark",
    )
