package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

/**
 * Finds classes in CSS `@apply` directives, which live in plain stylesheet text rather than
 * inside any string token the lexer produces.
 */
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

/** A directive ends where CSS says it does, not at the end of a line: wrapping a long list is idiomatic. */
private val TERMINATORS = charArrayOf(';', '}')

/**
 * How far a single directive is allowed to reach.
 *
 * A file being typed in holds unterminated rules, and without a bound one missing semicolon would
 * hand the rest of the file to the parser as though it were a class list.
 */
private const val MAX_DIRECTIVE_LENGTH = 500

private fun String.directiveEnd(start: Int): Int {
    val limit = (start + MAX_DIRECTIVE_LENGTH).coerceAtMost(length)
    var index = start

    while (index < limit && this[index] !in TERMINATORS) index++

    return index
}
