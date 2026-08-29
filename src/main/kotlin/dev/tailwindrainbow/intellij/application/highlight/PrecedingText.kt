package dev.tailwindrainbow.intellij.application.highlight

/** Walks out of the objects and arrays a token sits inside to the `=` that assigns the whole structure. */
internal fun String.assignmentBefore(
    tokenStart: Int,
    window: Int,
): Int? {
    val limit = (tokenStart - window).coerceAtLeast(0)
    var depth = 0
    var calls = 0
    var index = tokenStart - 1

    while (index >= limit) {
        when (this[index]) {
            '}', ']' -> depth++
            '{', '[' -> if (depth > 0) depth--
            ')' -> calls++
            '(' -> if (calls == 0) return null else calls--
            ';' -> if (depth == 0) return null
            '=' -> if (depth == 0 && calls == 0) return index.takeIf { assignsAt(it) }
        }
        index--
    }

    return null
}

private fun String.assignsAt(index: Int): Boolean {
    val before = getOrNull(index - 1)
    val after = getOrNull(index + 1)

    return before !in COMPARISON_HEADS && after != '=' && after != '>'
}

private val COMPARISON_HEADS = listOf('=', '!', '<', '>')

/** Both patterns end-anchor, so only a window ending in `=`, `:`, or an open `{`/`[` can match one. */
internal fun String.endsAnAssignment(): Boolean {
    val last = lastNonWhitespace(length) ?: return false
    if (this[last] == '=' || this[last] == ':') return true

    var insideBraces = true
    var insideBrackets = true

    for (index in length - 1 downTo 0) {
        when (this[index]) {
            '{' -> if (insideBraces) return true
            '}' -> insideBraces = false
            '[' -> if (insideBrackets) return true
            ']' -> insideBrackets = false
        }

        if (!insideBraces && !insideBrackets) return false
    }

    return false
}

private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_' || this == '$'

internal fun String.identifierBefore(endExclusive: Int): String? = identifierRangeBefore(endExclusive)?.let(::substring)

internal fun String.identifierRangeBefore(endExclusive: Int): IntRange? {
    val end = lastNonWhitespace(endExclusive) ?: return null

    var start = end
    while (start >= 0 && this[start].isIdentifierPart()) start--

    return if (start == end) null else start + 1..end
}

internal fun String.tagChainHead(): String? {
    var end = skipWhatTheTagCarries(length) ?: return null
    var head: String? = null

    while (true) {
        val identifier = identifierRangeBefore(end) ?: return head
        head = substring(identifier)

        val dot = identifier.first - 1
        if (dot < 0 || this[dot] != '.') return head

        end = skipWhatTheTagCarries(dot) ?: return head
    }
}

private fun String.skipWhatTheTagCarries(endExclusive: Int): Int? {
    var end = endExclusive

    while (true) {
        val last = lastNonWhitespace(end) ?: return null

        end =
            when (this[last]) {
                ')' -> openerOf(last, '(', ')') ?: return null
                '>' -> openerOf(last, '<', '>') ?: return null
                else -> return end
            }
    }
}

private fun String.openerOf(
    closeIndex: Int,
    opener: Char,
    closer: Char,
): Int? {
    var depth = 0

    for (index in closeIndex downTo 0) {
        when (this[index]) {
            closer -> depth++
            opener -> {
                depth--
                if (depth == 0) return index
            }
        }
    }

    return null
}

internal fun String.lastNonWhitespace(endExclusive: Int): Int? {
    var index = endExclusive - 1
    while (index >= 0 && this[index].isWhitespace()) index--

    return index.takeIf { it >= 0 }
}
