package dev.tailwindrainbow.intellij.application.highlight

internal enum class ClassContent {
    NONE,
    CLASS_NAMES,
    EXPRESSION,
}

internal class ClassContextDetector(private val settings: ScanSettings) {
    private val identifierAlternation = settings.classIdentifiers.identifierPattern()
    private val identifierWords = settings.classIdentifiers.searchWords()

    val attributeAssignment = identifierAlternation?.let { Regex("(?i)$it\\s*=\\s*(?<$QUOTE_GROUP>[\"'])") }

    private val attributeValue =
        identifierAlternation?.let { Regex("(?is)$it\\s*=\\s*(?:\\{[^{}]*|\\[[^\\[\\]]*)?$") }

    private val assignedValue = identifierAlternation?.let { Regex("(?is)$it\\s*(?:(?::[^=]+)?=|:)\\s*$") }

    private val assignedName = identifierAlternation?.let { Regex("(?i)$it") }

    fun classify(
        text: String,
        token: DocumentToken,
    ): ClassContent {
        val precedingText = text.substring((token.start - CONTEXT_WINDOW).coerceAtLeast(0), token.start)
        val couldBeAssigned = precedingText.endsAnAssignment() && namesAnIdentifier(precedingText)

        if (couldBeAssigned) {
            attributeValue?.find(precedingText)?.let { attribute ->
                return if (attribute.isBound) ClassContent.EXPRESSION else ClassContent.CLASS_NAMES
            }

            if (isNestedInAssignedValue(text, token.start)) {
                return ClassContent.CLASS_NAMES
            }
        }

        val holdsClassNames =
            (couldBeAssigned && assignedValue.matchesEndOf(precedingText)) ||
                isTaggedTemplate(precedingText, token) ||
                isClassHelperArgument(text, token.start)

        return if (holdsClassNames) ClassContent.CLASS_NAMES else ClassContent.NONE
    }

    private fun namesAnIdentifier(text: String) = identifierWords.any { text.contains(it, ignoreCase = true) }

    private fun isNestedInAssignedValue(
        text: String,
        tokenStart: Int,
    ): Boolean {
        val assignment = text.assignmentBefore(tokenStart, CONTEXT_WINDOW) ?: return false
        val name = text.assignedNameBefore(assignment, CONTEXT_WINDOW) ?: return false

        return assignedName?.matches(text.substring(name)) == true
    }

    private fun isTaggedTemplate(
        precedingText: String,
        token: DocumentToken,
    ): Boolean = token.kind == TokenKind.TEMPLATE && precedingText.tagChainHead() in settings.templateTags

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

private val BINDING_MARKERS = listOf("v-bind:", "x-bind:", ":")

private const val BINDING_GROUP = "binding"

internal const val QUOTE_GROUP = "quote"

private fun Set<String>.identifierPattern(): String? {
    if (isEmpty()) return null

    val exact =
        joinToString("|") { identifier ->
            if (identifier.endsWith(':')) "${Regex.escape(identifier)}[A-Za-z0-9_-]*" else Regex.escape(identifier)
        }
    val markers = BINDING_MARKERS.joinToString("|", transform = Regex::escape)

    return "(?<![\\w:-])(?<$BINDING_GROUP>$markers)?(?:$exact|${compoundPattern()})(?![\\w-])"
}

private fun Set<String>.compoundPattern(): String {
    val tails =
        filterNot { it.endsWith(':') }
            .joinToString("|") { Regex.escape(it.replaceFirstChar(Char::uppercaseChar)) }

    val caseSensitive = "[A-Za-z0-9_$]*[a-z0-9](?:$tails)"

    return "(?-i:$caseSensitive)"
}
