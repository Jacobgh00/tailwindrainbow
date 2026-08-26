package dev.tailwindrainbow.intellij.domain

class TailwindDocumentScanner {
    fun scan(
        text: String,
        fileExtension: String,
        settings: ScanSettings,
        theme: RainbowTheme,
    ): List<HighlightSegment> {
        if (text.length > settings.maxFileSize) {
            return emptyList()
        }

        if (fileExtension.lowercase() !in settings.supportedExtensions) {
            return emptyList()
        }

        val tokens = tokenize(text, hashComments = fileExtension.equals("php", ignoreCase = true))
        val parser = TailwindClassParser(ThemeMatcher(theme, settings.ignoredPrefixModifiers))

        return buildList {
            if (fileExtension.lowercase() in CSS_EXTENSIONS) {
                addAll(scanApplyDirectives(text, tokens, parser))
            }

            tokens.filter { it.kind != TokenKind.COMMENT }.forEach { token ->
                addAll(scanEmbeddedAttributes(token, settings, parser))

                if (isClassContext(text, token, settings)) {
                    addAll(parser.parse(token.content, token.contentStart))
                }
            }
        }.distinctBy { Triple(it.start, it.end, it.themeKey) }
            .sortedBy(HighlightSegment::start)
    }

    private fun scanEmbeddedAttributes(
        token: DocumentToken,
        settings: ScanSettings,
        parser: TailwindClassParser,
    ): List<HighlightSegment> = buildList {
        val identifier = settings.classIdentifiers.identifierPattern() ?: return@buildList
        val attribute = Regex("(?i)$identifier\\s*=\\s*([\"'])")

        attribute.findAll(token.content).forEach { match ->
            val quote = match.groupValues[1].single()
            val valueStart = match.range.last + 1
            val valueEnd = token.content.indexOfUnescaped(quote, valueStart)

            if (valueEnd >= valueStart) {
                addAll(parser.parse(token.content.substring(valueStart, valueEnd), token.contentStart + valueStart))
            }
        }
    }

    private fun isClassContext(text: String, token: DocumentToken, settings: ScanSettings): Boolean {
        val contextStart = (token.start - CONTEXT_WINDOW).coerceAtLeast(0)
        val before = text.substring(contextStart, token.start)

        return isAttributeValue(before, settings.classIdentifiers) ||
                isAssignedClassValue(before, settings.classIdentifiers) ||
                isInsideClassFunction(text, token.start, settings.classFunctions) ||
                isTaggedTemplate(before, token, settings.templateTags)
    }

    private fun isAttributeValue(before: String, identifiers: Set<String>): Boolean {
        val identifier = identifiers.identifierPattern() ?: return false
        return Regex("(?is)$identifier\\s*=\\s*(?:\\{[^{}]*)?$").containsMatchIn(before)
    }

    private fun isAssignedClassValue(before: String, identifiers: Set<String>): Boolean {
        val identifier = identifiers.identifierPattern() ?: return false
        return Regex("(?is)$identifier\\s*(?:(?::[^=]+)?=|:)\\s*$").containsMatchIn(before)
    }

    private fun isTaggedTemplate(before: String, token: DocumentToken, tags: Set<String>): Boolean {
        if (token.kind != TokenKind.TEMPLATE) return false
        val identifier = Regex("([A-Za-z_$][\\w$]*)\\s*$").find(before)?.groupValues?.get(1)
        return identifier in tags
    }

    private fun isInsideClassFunction(text: String, tokenStart: Int, functions: Set<String>): Boolean {
        var parenthesisDepth = 0
        var index = tokenStart - 1

        while (index >= 0 && tokenStart - index <= EXTENDED_CONTEXT_WINDOW) {
            when (text[index]) {
                ')' -> parenthesisDepth++
                '(' -> if (parenthesisDepth == 0) {
                    val functionName = text.identifierBefore(index)
                    if (functionName != null) return functionName in functions
                } else {
                    parenthesisDepth--
                }
            }
            index--
        }

        return false
    }

    private fun scanApplyDirectives(
        text: String,
        tokens: List<DocumentToken>,
        parser: TailwindClassParser,
    ): List<HighlightSegment> = buildList {
        val commentRanges = tokens.filter { it.kind == TokenKind.COMMENT }.map { it.start until it.end }
        var searchFrom = 0

        while (searchFrom < text.length) {
            val applyStart = text.indexOf("@apply", searchFrom)
            if (applyStart < 0) break
            if (commentRanges.any { applyStart in it }) {
                searchFrom = applyStart + APPLY.length
                continue
            }

            val contentStart = text.indexAfterWhitespace(applyStart + APPLY.length)
            val contentEnd = text.indexOfFirstOrEnd(contentStart, ';', '}', '\n', '\r')
            addAll(parser.parse(text.substring(contentStart, contentEnd), contentStart))
            searchFrom = contentEnd + 1
        }
    }

    private fun tokenize(text: String, hashComments: Boolean): List<DocumentToken> = buildList {
        var index = 0

        while (index < text.length) {
            when {
                text.startsWith("<!--", index) -> {
                    val end = text.commentEnd(index, "-->")
                    add(DocumentToken(TokenKind.COMMENT, "", index, end, index))
                    index = end
                }

                text.startsWith("{/*", index) -> {
                    val end = text.commentEnd(index, "*/}")
                    add(DocumentToken(TokenKind.COMMENT, "", index, end, index))
                    index = end
                }

                text.startsWith("/*", index) -> {
                    val end = text.commentEnd(index, "*/")
                    add(DocumentToken(TokenKind.COMMENT, "", index, end, index))
                    index = end
                }

                text.startsWith("//", index) || (hashComments && text[index] == '#') -> {
                    val end = text.lineEnd(index)
                    add(DocumentToken(TokenKind.COMMENT, "", index, end, index))
                    index = end
                }

                text[index] == '\'' || text[index] == '"' || text[index] == '`' -> {
                    val quote = text[index]
                    val end = text.indexOfUnescaped(quote, index + 1).let { if (it < 0) text.length else it }
                    val contentEnd = end.coerceAtMost(text.length)
                    val kind = if (quote == '`') TokenKind.TEMPLATE else TokenKind.STRING
                    add(DocumentToken(kind, text.substring(index + 1, contentEnd), index, (end + 1).coerceAtMost(text.length), index + 1))
                    index = (end + 1).coerceAtMost(text.length)
                }

                else -> index++
            }
        }
    }

    private companion object {
        const val APPLY = "@apply"
        const val CONTEXT_WINDOW = 500
        const val EXTENDED_CONTEXT_WINDOW = 2_000
        val CSS_EXTENSIONS = setOf("css", "scss", "sass", "less", "styl", "stylus", "pcss", "postcss")
    }
}

private enum class TokenKind {
    STRING,
    TEMPLATE,
    COMMENT,
}

private data class DocumentToken(
    val kind: TokenKind,
    val content: String,
    val start: Int,
    val end: Int,
    val contentStart: Int,
)

private fun String.indexOfUnescaped(character: Char, startIndex: Int): Int {
    var index = startIndex

    while (index < length) {
        if (this[index] == '\\') {
            index += 2
            continue
        }
        if (this[index] == character) return index
        index++
    }

    return -1
}

private fun String.commentEnd(start: Int, terminator: String): Int {
    val terminatorIndex = indexOf(terminator, start + 2)
    return if (terminatorIndex < 0) length else terminatorIndex + terminator.length
}

private fun String.lineEnd(start: Int): Int {
    val newLine = indexOf('\n', start)
    return if (newLine < 0) length else newLine
}

private fun String.identifierBefore(endExclusive: Int): String? {
    var end = endExclusive - 1
    while (end >= 0 && this[end].isWhitespace()) end--
    var start = end
    while (start >= 0 && (this[start].isLetterOrDigit() || this[start] == '_' || this[start] == '$')) start--
    return substring(start + 1, end + 1).ifEmpty { null }
}

private fun String.indexAfterWhitespace(start: Int): Int {
    var index = start
    while (index < length && this[index].isWhitespace()) index++
    return index
}

private fun String.indexOfFirstOrEnd(start: Int, vararg delimiters: Char): Int {
    var index = start
    while (index < length && this[index] !in delimiters) index++
    return index
}

private fun Set<String>.identifierPattern(): String? {
    if (isEmpty()) return null

    val alternatives = joinToString("|") { identifier ->
        if (identifier.endsWith(':')) {
            "${Regex.escape(identifier)}[A-Za-z0-9_-]*"
        } else {
            Regex.escape(identifier)
        }
    }

    return "(?<![\\w:-])(?:$alternatives)(?![\\w-])"
}
