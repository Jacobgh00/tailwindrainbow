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

    /**
     * A class identifier assigned a value: `class="…"`, `class={…}`, and `classes = ['…', …]`.
     *
     * The value may be a collection the token sits inside. Each opener excludes only its own closer,
     * so a match reaches into the collection that was opened but never past the end of it.
     */
    private val attributeValue =
        identifierAlternation?.let { Regex("(?is)$it\\s*=\\s*(?:\\{[^{}]*|\\[[^\\[\\]]*)?$") }

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
    ): Boolean = token.kind == TokenKind.TEMPLATE && precedingText.tagChainHead() in settings.templateTags

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
                        return isClassHelperCall(text, index)
                    } else {
                        depth--
                    }
            }
            index--
        }

        return false
    }

    /**
     * Whether the call opening at [callIndex] is one that takes class names.
     *
     * Either the function itself is configured — `clsx(…)` — or it is a method on something that is,
     * which is what `classList.add(…)` and `el.classList.toggle(…)` are. The function is tried first,
     * so a configured name still decides on its own.
     */
    private fun isClassHelperCall(
        text: String,
        callIndex: Int,
    ): Boolean {
        val callee = text.identifierRangeBefore(callIndex) ?: return false
        if (text.substring(callee) in settings.classFunctions) return true

        val dot = callee.first - 1
        return dot >= 0 && text[dot] == '.' && text.identifierBefore(dot) in settings.classFunctions
    }

    private companion object {
        const val CONTEXT_WINDOW = 500
        const val HELPER_SEARCH_WINDOW = 2_000
    }
}

private val MatchResult.isBound: Boolean get() = groups[BINDING_GROUP] != null

private fun Regex?.matchesEndOf(text: String): Boolean = this?.containsMatchIn(text) == true

private fun Char.isIdentifierPart(): Boolean = isLetterOrDigit() || this == '_' || this == '$'

private fun String.identifierBefore(endExclusive: Int): String? = identifierRangeBefore(endExclusive)?.let(::substring)

/** Where the identifier ending at [endExclusive] begins, ignoring whitespace between the two. */
private fun String.identifierRangeBefore(endExclusive: Int): IntRange? {
    val end = lastNonWhitespace(endExclusive) ?: return null

    var start = end
    while (start >= 0 && this[start].isIdentifierPart()) start--

    return if (start == end) null else start + 1..end
}

/**
 * The identifier a tagged template hangs off, looking past whatever the tag carries: `` styled.div` ``,
 * `` styled(Button)` ``, `` styled.div<Props>` ``, and `` styled.input.attrs({…})` `` all hang off
 * `styled`.
 *
 * The head is what counts, not the member: `styled.div` is a styled-components template, while
 * `logger.css` is a call on a logger that happens to share a name with a tag.
 */
private fun String.tagChainHead(): String? {
    var end = skipWhatTheTagCarries(length) ?: return null
    var head: String? = null

    while (true) {
        val identifier = identifierRangeBefore(end) ?: return head
        head = substring(identifier)

        // Keep walking only while the chain continues leftwards through a member access.
        val dot = identifier.first - 1
        if (dot < 0 || this[dot] != '.') return head

        end = skipWhatTheTagCarries(dot) ?: return head
    }
}

/**
 * Where the identifier before [endExclusive] ends, once the component, type arguments, or attributes
 * a tag may carry are stepped over: `(Button)`, `<Props>`, `({ type: 'text' })`.
 */
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

/** The index of the bracket that opens the one at [closeIndex], honouring nesting. */
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

private fun String.lastNonWhitespace(endExclusive: Int): Int? {
    var index = endExclusive - 1
    while (index >= 0 && this[index].isWhitespace()) index--

    return index.takeIf { it >= 0 }
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
 * Builds one alternation matching any configured class identifier, bound or plain, alone or as the
 * tail of a compound name.
 *
 * Identifiers ending in `:` (Svelte's `class:`, Vue's `className:`) also match their directive
 * suffix, so `class:active` is recognised. Returns null when nothing is configured.
 *
 * The marker is consumed by the pattern rather than allowed by the lookbehind, which is what keeps
 * `foo:class` and `:superclass` out while letting `:class` in.
 */
private fun Set<String>.identifierPattern(): String? {
    if (isEmpty()) return null

    val exact =
        joinToString("|") { identifier ->
            if (identifier.endsWith(':')) "${Regex.escape(identifier)}[A-Za-z0-9_-]*" else Regex.escape(identifier)
        }
    val markers = BINDING_MARKERS.joinToString("|", transform = Regex::escape)

    return "(?<![\\w:-])(?<$BINDING_GROUP>$markers)?(?:$exact|${compoundPattern()})(?![\\w-])"
}

/**
 * Matches a name that ends in a class identifier across a camel case boundary: `buttonClasses`,
 * `cardClassName`, `wrapperClass`.
 *
 * The boundary is what makes this safe to allow. Case matters here and nowhere else in the pattern —
 * hence `(?-i:…)` — because `superclass` and `subclass` are ordinary words that must keep meaning
 * nothing, while `buttonClasses` reads as a list of classes to anyone.
 */
private fun Set<String>.compoundPattern(): String {
    val tails =
        filterNot { it.endsWith(':') }
            .joinToString("|") { Regex.escape(it.replaceFirstChar(Char::uppercaseChar)) }

    return "(?-i:[A-Za-z0-9_$]*[a-z0-9](?:$tails))"
}
