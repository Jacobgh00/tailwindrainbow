package dev.tailwindrainbow.intellij.application.variants

fun variantsDeclaredIn(text: String): Set<String> =
    buildSet {
        addAll(CUSTOM_VARIANT.names(text))
        addAll(BREAKPOINT_TOKEN.names(text))
        addAll(ADD_VARIANT_CALL.names(text))
        addAll(screenNames(text))
    }

private val CUSTOM_VARIANT = Regex("@custom-variant\\s+([A-Za-z][\\w-]*)")

private val BREAKPOINT_TOKEN = Regex("--breakpoint-([A-Za-z][\\w-]*)\\s*:")

private val ADD_VARIANT_CALL = Regex("addVariant\\(\\s*['\"]([^'\"]+)['\"]")

private val SCREENS_BLOCK = Regex("screens\\s*:\\s*\\{")

private val SCREEN_KEY = Regex("['\"]?([A-Za-z][\\w-]*)['\"]?\\s*:")

private fun Regex.names(text: String): List<String> = findAll(text).map { it.groupValues[1] }.toList()

private fun screenNames(text: String): List<String> =
    SCREENS_BLOCK.findAll(text).flatMap { block ->
        val keys = text.blockAfter(block.range.last) ?: return@flatMap emptySequence()

        SCREEN_KEY.findAll(keys).map { it.groupValues[1] }
    }.toList()

private fun String.blockAfter(openingBrace: Int): String? {
    var depth = 1
    var index = openingBrace + 1
    val limit = (index + MAX_BLOCK_LENGTH).coerceAtMost(length)

    while (index < limit) {
        when (this[index]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) return substring(openingBrace + 1, index)
            }
        }
        index++
    }

    return null
}

private const val MAX_BLOCK_LENGTH = 2_000
