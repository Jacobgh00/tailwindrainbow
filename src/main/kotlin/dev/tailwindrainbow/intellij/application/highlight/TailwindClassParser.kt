package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatch
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher

class TailwindClassParser(private val themeMatcher: ThemeMatcher) {
    fun parse(
        content: String,
        startOffset: Int = 0,
    ): List<HighlightSegment> =
        buildList {
            for ((value, start) in content.classWords()) {
                addAll(parseClass(value, startOffset + start))
            }
        }

    private fun parseClass(
        className: String,
        startOffset: Int,
    ): List<HighlightSegment> {
        if (className.startsWith(':') || className.endsWith(':')) {
            return emptyList()
        }

        val (parts, importantAt) = className.splitOnUnnestedColons(startOffset).takeImportantOut()

        if (parts.any { it.value.isEmpty() }) {
            return emptyList()
        }

        return buildList {
            addImportantSegment(importantAt)
            addClassSegments(parts, importantAt)
        }
    }

    private fun MutableList<HighlightSegment>.addImportantSegment(importantAt: Int?) {
        if (importantAt == null) {
            return
        }

        themeMatcher.matchImportant()?.let { match ->
            add(match.toSegment(importantAt, importantAt + 1))
        }
    }

    private fun MutableList<HighlightSegment>.addClassSegments(
        parts: List<ClassPart>,
        importantAt: Int?,
    ) {
        val baseClass = parts.last()
        val prefixes = parts.dropLast(1)
        val baseMatch = themeMatcher.matchBase(baseClass.value)

        prefixes.forEachIndexed { index, prefix ->
            themeMatcher.matchPrefix(prefix.value)?.let { match ->
                val hasFollowingStyledSegment = index < prefixes.lastIndex || baseMatch != null
                val end = if (hasFollowingStyledSegment) prefix.end + 1 else baseClass.end

                addSegmentAround(importantAt, match, prefix.offset, end)
            }
        }

        baseMatch?.let { match ->
            add(match.toSegment(baseClass.offset, baseClass.end))
        }
    }

    /**
     * Adds a segment, stepping around the important marker if it sits inside.
     *
     * The last prefix colours everything after it when the utility has no colour of its own, and in
     * `hover:!font-bold` the marker sits in the middle of exactly that stretch. It has a colour of
     * its own, so the stretch is painted either side of it rather than over it.
     */
    private fun MutableList<HighlightSegment>.addSegmentAround(
        importantAt: Int?,
        match: ThemeMatch,
        start: Int,
        end: Int,
    ) {
        if (importantAt == null || importantAt !in start until end) {
            add(match.toSegment(start, end))
            return
        }

        if (start < importantAt) add(match.toSegment(start, importantAt))
        if (importantAt + 1 < end) add(match.toSegment(importantAt + 1, end))
    }
}

/** One colon-separated piece of a class, with where it sits in the document. */
private data class ClassPart(val value: String, val offset: Int) {
    val end: Int get() = offset + value.length

    fun withoutFirstCharacter() = ClassPart(value.drop(1), offset + 1)

    fun withoutLastCharacter() = ClassPart(value.dropLast(1), offset)
}

/** A class with its important marker taken out, and where that marker was. */
private data class MarkedClass(val parts: List<ClassPart>, val importantAt: Int?)

/**
 * Separates the important marker from the class it marks.
 *
 * Tailwind has spelled it three ways: before the whole class and before the utility in v3
 * (`!font-bold`, `hover:!font-bold`), and after the utility in v4 (`hover:font-bold!`). All three
 * mean the same thing, and codebases hold a mixture during a migration, so all three are read.
 */
private fun List<ClassPart>.takeImportantOut(): MarkedClass {
    val first = first()
    val last = last()

    return when {
        first.value.startsWith(IMPORTANT) -> MarkedClass(replacingFirst(first.withoutFirstCharacter()), first.offset)
        last.value.endsWith(IMPORTANT) -> MarkedClass(replacingLast(last.withoutLastCharacter()), last.end - 1)
        last.value.startsWith(IMPORTANT) -> MarkedClass(replacingLast(last.withoutFirstCharacter()), last.offset)
        else -> MarkedClass(this, null)
    }
}

private const val IMPORTANT = '!'

private fun List<ClassPart>.replacingFirst(part: ClassPart) = listOf(part) + drop(1)

private fun List<ClassPart>.replacingLast(part: ClassPart) = dropLast(1) + part

private data class ClassWord(val value: String, val start: Int)

private fun String.classWords(): List<ClassWord> {
    val content = this

    return buildList {
        var wordStart = -1
        var bracketDepth = 0

        for (index in 0..content.length) {
            val character = content.getOrNull(index)

            when (character) {
                '[' -> bracketDepth++
                ']' -> bracketDepth = (bracketDepth - 1).coerceAtLeast(0)
            }

            val isBoundary = character == null || (bracketDepth == 0 && character.isWhitespace())

            if (!isBoundary && wordStart < 0) wordStart = index

            if (isBoundary && wordStart >= 0) {
                add(ClassWord(content.substring(wordStart, index), wordStart))
                wordStart = -1
            }
        }
    }
}

private fun String.splitOnUnnestedColons(startOffset: Int): List<ClassPart> {
    val className = this

    return buildList {
        var partStart = 0
        var bracketDepth = 0

        className.forEachIndexed { index, character ->
            when (character) {
                '[' -> bracketDepth++
                ']' -> bracketDepth = (bracketDepth - 1).coerceAtLeast(0)
                ':' ->
                    if (bracketDepth == 0) {
                        add(ClassPart(className.substring(partStart, index), startOffset + partStart))
                        partStart = index + 1
                    }
            }
        }

        add(ClassPart(className.substring(partStart), startOffset + partStart))
    }
}

private fun ThemeMatch.toSegment(
    start: Int,
    end: Int,
): HighlightSegment = HighlightSegment(start, end, key, style, kind)
