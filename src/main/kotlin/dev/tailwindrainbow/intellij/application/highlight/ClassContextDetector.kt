package dev.tailwindrainbow.intellij.application.highlight

/**
 * What a lexed token holds, as far as class names are concerned.
 *
 * A bound attribute is told apart from a plain one because `:class="[…]"` is an expression: the
 * class names are the strings inside it, and the brackets, commas, and variable names around them
 * are not class names at all.
 */
internal enum class ClassContent {
    NONE,
    CLASS_NAMES,
    EXPRESSION,
}

/**
 * Decides whether a lexed token holds Tailwind class names.
 *
 * The lexer cannot tell `class="p-4"` from any other string, so this is where the heuristics live:
 * an attribute value, a value assigned to a class-shaped key, an argument to a class helper such as
 * `clsx(...)`, or a tagged template like `` tw`...` ``.
 *
 * Regexes are compiled once per instance because they depend only on settings, which change on
 * Apply, while a large document yields thousands of tokens.
 */
internal class ClassContextDetector(private val settings: ScanSettings) {
    private val identifierAlternation = settings.classIdentifiers.identifierPattern()

    /** `class="…"` including the opening quote, used to find attributes nested inside a token. */
    val attributeAssignment = identifierAlternation?.let { Regex("(?i)$it\\s*=\\s*(?<$QUOTE_GROUP>[\"'])") }

    private val attributeValue = identifierAlternation?.let { Regex("(?is)$it\\s*=\\s*(?:\\{[^{}]*)?$") }

    private val assignedValue = identifierAlternation?.let { Regex("(?is)$it\\s*(?:(?::[^=]+)?=|:)\\s*$") }

    fun classify(
        text: String,
        token: DocumentToken,
    ): ClassContent {
        val precedingText = text.substring((token.start - CONTEXT_WINDOW).coerceAtLeast(0), token.start)

        attributeValue?.find(precedingText)?.let { attribute ->
            return if (attribute.isBound) ClassContent.EXPRESSION else ClassContent.CLASS_NAMES
        }

        val holdsClassNames =
            assignedValue.matchesEndOf(precedingText) ||
                isTaggedTemplate(precedingText, token) ||
                isClassHelperArgument(text, token.start)

        return if (holdsClassNames) ClassContent.CLASS_NAMES else ClassContent.NONE
    }

    private fun isTaggedTemplate(
        precedingText: String,
        token: DocumentToken,
    ): Boolean {
        if (token.kind != TokenKind.TEMPLATE) return false

        val tag = TRAILING_IDENTIFIER.find(precedingText)?.groupValues?.get(1)
        return tag in settings.templateTags
    }

    /** Walks left past balanced parentheses to the call this token sits inside, if any. */
    private fun isClassHelperArgument(
        text: String,
        tokenStart: Int,
    ): Boolean {
        var depth = 0
        var index = tokenStart - 1

        while (index >= 0 && tokenStart - index <= HELPER_SEARCH_WINDOW) {
            when (text[index]) {
                ')' -> depth++
                '(' ->
                    if (depth == 0) {
                        val callee = text.identifierBefore(index) ?: return false
                        return callee in settings.classFunctions
                    } else {
                        depth--
                    }
            }
            index--
        }

        return false
    }

    private companion object {
        const val CONTEXT_WINDOW = 500
        const val HELPER_SEARCH_WINDOW = 2_000
        val TRAILING_IDENTIFIER = Regex("([A-Za-z_$][\\w$]*)\\s*$")
    }
}

private val MatchResult.isBound: Boolean get() = groups[BINDING_GROUP] != null

private fun Regex?.matchesEndOf(text: String): Boolean = this?.containsMatchIn(text) == true

private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_' || this == '$'

private fun String.identifierBefore(endExclusive: Int): String? {
    var end = endExclusive - 1
    while (end >= 0 && this[end].isWhitespace()) end--

    var start = end
    while (start >= 0 && this[start].isIdentifierPart()) start--

    return substring(start + 1, end + 1).ifEmpty { null }
}

/**
 * Frameworks bind an attribute instead of assigning it: Vue's `:class`, its long form
 * `v-bind:class`, and Alpine's `x-bind:class` all name the same attribute. The marker is part of the
 * binding syntax rather than part of the attribute, so a user configures `class` once and every
 * bound spelling of it follows.
 */
private val BINDING_MARKERS = listOf("v-bind:", "x-bind:", ":")

private const val BINDING_GROUP = "binding"

/** Groups are read by name: the optional binding marker makes their numbering unstable. */
internal const val QUOTE_GROUP = "quote"

/**
 * Builds one alternation matching any configured class identifier, bound or plain.
 *
 * Identifiers ending in `:` (Svelte's `class:`, Vue's `className:`) also match their directive
 * suffix, so `class:active` is recognised. Returns null when nothing is configured.
 *
 * The marker is consumed by the pattern rather than allowed by the lookbehind, which is what keeps
 * `foo:class` and `:superclass` out while letting `:class` in.
 */
private fun Set<String>.identifierPattern(): String? {
    if (isEmpty()) return null

    val alternatives =
        joinToString("|") { identifier ->
            if (identifier.endsWith(':')) "${Regex.escape(identifier)}[A-Za-z0-9_-]*" else Regex.escape(identifier)
        }
    val markers = BINDING_MARKERS.joinToString("|", transform = Regex::escape)

    return "(?<![\\w:-])(?<$BINDING_GROUP>$markers)?(?:$alternatives)(?![\\w-])"
}
