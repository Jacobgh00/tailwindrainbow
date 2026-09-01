package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.ModifierSegment
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
            add(match.toSegment(importantAt, importantAt + 1, MatchSpan(importantAt, importantAt + 1)))
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
            val parts = themeMatcher.matchPrefixParts(prefix.value)
            val visualStart = addModifierSegments(importantAt, parts.modifiers, prefix.offset)
            val matchSpan = MatchSpan(prefix.offset + parts.scopingModifierWidth, prefix.end)

            parts.variant?.let { match ->
                val hasFollowingStyledSegment = index < prefixes.lastIndex || baseMatch != null
                val end = if (hasFollowingStyledSegment) prefix.end + 1 else baseClass.end

                addSegmentAround(importantAt, match, visualStart, end, matchSpan)
            }
        }

        baseMatch?.let { match ->
            add(
                match.toSegment(
                    baseClass.offset,
                    baseClass.end,
                    MatchSpan(baseClass.offset, baseClass.end),
                ),
            )
        }
    }

    private fun MutableList<HighlightSegment>.addModifierSegments(
        importantAt: Int?,
        modifiers: List<ModifierSegment>,
        offset: Int,
    ): Int =
        modifiers.fold(offset) { start, modifier ->
            modifier.match?.let {
                addSegmentAround(
                    importantAt,
                    it,
                    start,
                    start + modifier.width,
                    MatchSpan(start, start + modifier.width),
                )
            }
            start + modifier.width
        }

    private fun MutableList<HighlightSegment>.addSegmentAround(
        importantAt: Int?,
        match: ThemeMatch,
        start: Int,
        end: Int,
        matchSpan: MatchSpan,
    ) {
        if (importantAt == null || importantAt !in start until end) {
            add(match.toSegment(start, end, matchSpan))
            return
        }

        if (start < importantAt) add(match.toSegment(start, importantAt, matchSpan))
        if (importantAt + 1 < end) add(match.toSegment(importantAt + 1, end, matchSpan))
    }
}

internal data class ClassPart(val value: String, val offset: Int) {
    val end: Int get() = offset + value.length

    fun withoutFirstCharacter() = ClassPart(value.drop(1), offset + 1)

    fun withoutLastCharacter() = ClassPart(value.dropLast(1), offset)
}

private data class MarkedClass(val parts: List<ClassPart>, val importantAt: Int?)

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

private data class MatchSpan(val start: Int, val end: Int)

private fun List<ClassPart>.replacingFirst(part: ClassPart) = listOf(part) + drop(1)

private fun List<ClassPart>.replacingLast(part: ClassPart) = dropLast(1) + part

internal data class ClassWord(val value: String, val start: Int)

internal fun String.classWords(): List<ClassWord> {
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

internal fun String.splitOnUnnestedColons(startOffset: Int): List<ClassPart> {
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
    matchSpan: MatchSpan,
): HighlightSegment = HighlightSegment(start, end, matchSpan.start, matchSpan.end, key, style, kind)
