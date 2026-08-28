package dev.tailwindrainbow.intellij.application.variants

/**
 * The variant names a project declares for itself.
 *
 * Tailwind offers four ways to add one, across two major versions: `@custom-variant` and a
 * `--breakpoint-*` token in v4 CSS, `addVariant(…)` and a `screens` block in a v3 config file. All
 * four are read, because a codebase mid-migration has both and neither version's shape is a
 * superset of the other's.
 *
 * This is deliberately textual. Reading a v3 config exactly would mean running JavaScript, and a
 * declaration that hides behind a computed value or an import is one this cannot see — a name
 * missed costs the user a trip to the theme editor, which is what they would have done anyway.
 */
fun variantsDeclaredIn(text: String): Set<String> =
    buildSet {
        addAll(CUSTOM_VARIANT.names(text))
        addAll(BREAKPOINT_TOKEN.names(text))
        addAll(ADD_VARIANT_CALL.names(text))
        addAll(screenNames(text))
    }

/** `@custom-variant pointer-coarse (@media (pointer: coarse));` */
private val CUSTOM_VARIANT = Regex("@custom-variant\\s+([A-Za-z][\\w-]*)")

/** `@theme { --breakpoint-tablet: 40rem; }`, which names a responsive variant. */
private val BREAKPOINT_TOKEN = Regex("--breakpoint-([A-Za-z][\\w-]*)\\s*:")

/** `addVariant('supports-grid', '@supports (display: grid)')` */
private val ADD_VARIANT_CALL = Regex("addVariant\\(\\s*['\"]([^'\"]+)['\"]")

/** The opening of a `screens` block, whose keys are the project's responsive variants. */
private val SCREENS_BLOCK = Regex("screens\\s*:\\s*\\{")

/** A key inside that block, quoted or bare, ignoring the value that follows it. */
private val SCREEN_KEY = Regex("['\"]?([A-Za-z][\\w-]*)['\"]?\\s*:")

private fun Regex.names(text: String): List<String> = findAll(text).map { it.groupValues[1] }.toList()

private fun screenNames(text: String): List<String> =
    SCREENS_BLOCK.findAll(text).flatMap { block ->
        val keys = text.blockAfter(block.range.last) ?: return@flatMap emptySequence()

        SCREEN_KEY.findAll(keys).map { it.groupValues[1] }
    }.toList()

/**
 * The text of a brace block that opens at [openingBrace], honouring nesting.
 *
 * Bounded, and null when the block is never closed: a config file being typed in is not a reason to
 * read the rest of the file as screen names.
 */
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
