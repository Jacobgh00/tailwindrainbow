package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

/**
 * Finds classes in CSS `@apply` directives, which live in plain stylesheet text rather than
 * inside any string token the lexer produces.
 */
internal class ApplyDirectiveScanner(private val parser: TailwindClassParser) {
    fun scan(text: String, comments: List<DocumentToken>): List<HighlightSegment> = buildList {
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
            val classesEnd = text.indexOfAny(TERMINATORS, classesStart)

            addAll(parser.parse(text.substring(classesStart, classesEnd), classesStart))
            searchFrom = classesEnd + 1
        }
    }

    private companion object {
        const val APPLY = "@apply"
        val TERMINATORS = charArrayOf(';', '}', '\n', '\r')
    }
}

private fun String.indexAfterWhitespace(start: Int): Int {
    var index = start
    while (index < length && this[index].isWhitespace()) index++
    return index
}

private fun String.indexOfAny(characters: CharArray, start: Int): Int {
    var index = start
    while (index < length && this[index] !in characters) index++
    return index
}

