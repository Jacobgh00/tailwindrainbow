package dev.tailwindrainbow.intellij.application.highlight

internal fun String.assignedNameBefore(
    assignmentIndex: Int,
    window: Int,
): IntRange? =
    identifierRangeBefore(annotationBefore(assignmentIndex, window) ?: assignmentIndex)
        ?.takeIf { getOrNull(it.first - 1) !in NAME_CONTINUATIONS }

private fun String.annotationBefore(
    assignmentIndex: Int,
    window: Int,
): Int? {
    val limit = (assignmentIndex - window).coerceAtLeast(0)
    var depth = 0
    var annotation: Int? = null
    var index = assignmentIndex - 1

    while (index >= limit) {
        val character = this[index]
        val nesting = character.nestingDelta()

        when {
            arrowEndsAt(index) -> index--
            depth > 0 -> depth += nesting
            nesting < 0 -> return annotation
            nesting > 0 -> depth = 1
            character == ':' -> annotation = index
            character in STATEMENT_HEADS -> return annotation
        }

        index--
    }

    return annotation
}

private fun String.arrowEndsAt(index: Int): Boolean = this[index] == '>' && getOrNull(index - 1) == '='

private fun Char.nestingDelta(): Int =
    when (this) {
        ')', ']', '}', '>' -> 1
        '(', '[', '{', '<' -> -1
        else -> 0
    }

private val STATEMENT_HEADS = listOf(';', ',', '=')

private val NAME_CONTINUATIONS = listOf('-', ':')
