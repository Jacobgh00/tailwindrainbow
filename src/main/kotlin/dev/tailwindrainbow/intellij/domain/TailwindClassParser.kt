package dev.tailwindrainbow.intellij.domain

class TailwindClassParser(private val themeMatcher: ThemeMatcher) {
    fun parse(content: String, startOffset: Int = 0): List<HighlightSegment> = buildList {
        for ((value, start) in content.classWords()) {
            addAll(parseClass(value, startOffset + start))
        }
    }

    private fun parseClass(className: String, startOffset: Int): List<HighlightSegment> {
        if (className.startsWith(':') || className.endsWith(':')) {
            return emptyList()
        }

        val importantLength = if (className.startsWith('!')) 1 else 0
        val classWithoutImportant = className.drop(importantLength)
        val parts = classWithoutImportant.splitOnUnnestedColons()

        if (parts.isEmpty() || parts.any(String::isEmpty)) {
            return emptyList()
        }

        return buildList {
            addImportantSegment(startOffset, importantLength)
            addClassSegments(parts, startOffset + importantLength)
        }
    }

    private fun MutableList<HighlightSegment>.addImportantSegment(startOffset: Int, importantLength: Int) {
        if (importantLength == 0) {
            return
        }

        themeMatcher.matchImportant()?.let { match ->
            add(match.toSegment(startOffset, startOffset + 1, SegmentKind.IMPORTANT))
        }
    }

    private fun MutableList<HighlightSegment>.addClassSegments(parts: List<String>, startOffset: Int) {
        val baseClass = parts.last()
        val prefixes = parts.dropLast(1)
        val baseMatch = themeMatcher.matchBase(baseClass)

        if (prefixes.isEmpty()) {
            baseMatch?.let { add(it.toSegment(startOffset, startOffset + baseClass.length, it.segmentKind())) }
            return
        }

        val colonCount = prefixes.size
        val classEnd = startOffset + parts.sumOf(String::length) + colonCount
        var prefixStart = startOffset

        prefixes.forEachIndexed { index, prefix ->
            themeMatcher.matchPrefix(prefix)?.let { match ->
                val hasFollowingStyledSegment = index < prefixes.lastIndex || baseMatch != null
                val end = if (hasFollowingStyledSegment) prefixStart + prefix.length + 1 else classEnd

                add(match.toSegment(prefixStart, end, match.segmentKind(SegmentKind.PREFIX)))
            }

            prefixStart += prefix.length + 1
        }

        baseMatch?.let { match ->
            add(match.toSegment(prefixStart, prefixStart + baseClass.length, match.segmentKind()))
        }
    }
}

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

private fun String.splitOnUnnestedColons(): List<String> {
    val className = this

    return buildList {
        var partStart = 0
        var bracketDepth = 0

        className.forEachIndexed { index, character ->
            when (character) {
                '[' -> bracketDepth++
                ']' -> bracketDepth = (bracketDepth - 1).coerceAtLeast(0)
                ':' -> if (bracketDepth == 0) {
                    add(className.substring(partStart, index))
                    partStart = index + 1
                }
            }
        }

        add(className.substring(partStart))
    }
}

private fun ThemeMatch.toSegment(start: Int, end: Int, kind: SegmentKind): HighlightSegment =
    HighlightSegment(start, end, key, style, kind)

private fun ThemeMatch.segmentKind(default: SegmentKind = SegmentKind.BASE): SegmentKind =
    if (key == "arbitrary") SegmentKind.ARBITRARY else default