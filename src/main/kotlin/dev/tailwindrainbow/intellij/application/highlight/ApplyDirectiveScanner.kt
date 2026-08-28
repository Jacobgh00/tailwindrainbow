package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

internal class ApplyDirectiveScanner(private val parser: TailwindClassParser) {
    fun scan(
        text: String,
        comments: List<DocumentToken>,
    ): List<HighlightSegment> =
        buildList {
            val commentRanges = comments.map { it.start until it.end }
            var searchFrom = 0

            while (true) {
                val directiveStart = text.indexOf(APPLY, searchFrom)
                if (directiveStart < 0) return@buildList

                if (commentRanges.any { directiveStart in it }) {
                    searchFrom = directiveStart + APPLY.length
                    continue
                }

                val classesStart = text.indexAfterWhitespace(directiveStart + APPLY.length)
                val classesEnd = text.directiveEnd(classesStart)

                addAll(parser.parse(text.substring(classesStart, classesEnd), classesStart))
                searchFrom = classesEnd + 1
            }
        }

    private companion object {
        const val APPLY = "@apply"
    }
}

private fun String.indexAfterWhitespace(start: Int): Int {
    var index = start
    while (index < length && this[index].isWhitespace()) index++
    return index
}

private val TERMINATORS = charArrayOf(';', '}')

private const val MAX_DIRECTIVE_LENGTH = 500

private fun String.directiveEnd(start: Int): Int {
    val limit = (start + MAX_DIRECTIVE_LENGTH).coerceAtMost(length)
    var index = start

    while (index < limit && this[index] !in TERMINATORS) index++

    return index
}
