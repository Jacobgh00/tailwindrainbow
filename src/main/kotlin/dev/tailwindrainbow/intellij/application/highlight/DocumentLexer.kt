package dev.tailwindrainbow.intellij.application.highlight

internal enum class TokenKind {
    STRING,
    TEMPLATE,
    COMMENT,
}

/**
 * A string, template literal, or comment found in a document.
 *
 * [start]/[end] span the token including its delimiters; [contentStart] is where [content] begins,
 * so segment offsets stay relative to the document rather than to the token.
 */
internal data class DocumentToken(
    val kind: TokenKind,
    val content: String,
    val start: Int,
    val end: Int,
    val contentStart: Int,
)

/**
 * Splits raw document text into string, template, and comment tokens.
 *
 * Deliberately language-agnostic: recognising every host grammar is impossible, so the lexer
 * over-collects and [ClassContextDetector] decides which tokens are worth parsing.
 */
internal class DocumentLexer(private val profile: SyntaxProfile) {
    fun tokenize(text: String): List<DocumentToken> =
        buildList {
            var index = 0

            while (index < text.length) {
                val token = text.commentAt(index) ?: text.stringAt(index)

                if (token == null) {
                    index++
                } else {
                    add(token)
                    index = token.end
                }
            }
        }

    private fun String.commentAt(index: Int): DocumentToken? {
        val end =
            when {
                startsWith("<!--", index) -> commentEnd(index, "-->")
                startsWith("{/*", index) -> commentEnd(index, "*/}")
                startsWith("/*", index) -> commentEnd(index, "*/")
                startsWith("//", index) || (profile.usesHashComments && this[index] == '#') -> lineEnd(index)
                else -> return null
            }

        return DocumentToken(TokenKind.COMMENT, "", index, end, index)
    }

    private fun String.stringAt(index: Int): DocumentToken? {
        val quote = this[index]

        if (quote !in QUOTES) {
            return null
        }

        val closing = indexOfUnescaped(quote, index + 1)
        val contentEnd = if (closing < 0) length else closing
        val tokenEnd = (contentEnd + 1).coerceAtMost(length)

        return DocumentToken(
            kind = if (quote == '`') TokenKind.TEMPLATE else TokenKind.STRING,
            content = substring(index + 1, contentEnd),
            start = index,
            end = tokenEnd,
            contentStart = index + 1,
        )
    }

    private companion object {
        val QUOTES = charArrayOf('\'', '"', '`')
    }
}

internal fun String.indexOfUnescaped(
    character: Char,
    startIndex: Int,
): Int {
    var index = startIndex

    while (index < length) {
        when {
            this[index] == '\\' -> index += 2
            this[index] == character -> return index
            else -> index++
        }
    }

    return -1
}

private fun String.commentEnd(
    start: Int,
    terminator: String,
): Int {
    val terminatorIndex = indexOf(terminator, start + 2)

    if (terminatorIndex < 0) {
        return length
    }

    return terminatorIndex + terminator.length
}

private fun String.lineEnd(start: Int): Int {
    val newLine = indexOf('\n', start)

    if (newLine < 0) {
        return length
    }

    return newLine
}
