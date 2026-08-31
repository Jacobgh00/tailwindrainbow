package dev.tailwindrainbow.intellij.application.variants

fun variantsDeclaredIn(text: String): Set<String> {
    return variantDeclarationsIn(text).mapTo(linkedSetOf(), VariantDeclaration::name)
}

fun variantDeclarationsIn(
    text: String,
    sourcePath: String? = null,
): List<VariantDeclaration> =
    buildList {
        addAll(CUSTOM_VARIANT.declarations(text, VariantDeclarationKind.CUSTOM_VARIANT, sourcePath))
        addAll(BREAKPOINT_TOKEN.declarations(text, VariantDeclarationKind.BREAKPOINT, sourcePath))
        addAll(ADD_VARIANT_CALL.declarations(text, VariantDeclarationKind.ADD_VARIANT, sourcePath))
        addAll(screenDeclarations(text, sourcePath))
    }.sortedBy { it.location?.startOffset ?: Int.MAX_VALUE }

private val CUSTOM_VARIANT = Regex("@custom-variant\\s+([A-Za-z][\\w-]*)")

private val BREAKPOINT_TOKEN = Regex("--breakpoint-([A-Za-z][\\w-]*)\\s*:")

private val ADD_VARIANT_CALL = Regex("addVariant\\(\\s*['\"]([^'\"]+)['\"]")

private val SCREENS_BLOCK = Regex("screens\\s*:\\s*\\{")

private val SCREEN_KEY = Regex("['\"]?([A-Za-z][\\w-]*)['\"]?\\s*:")

private fun Regex.declarations(
    text: String,
    kind: VariantDeclarationKind,
    sourcePath: String?,
): List<VariantDeclaration> =
    findAll(text).map { match ->
        VariantDeclaration(
            name = match.groupValues[1],
            kind = kind,
            location = match.locationOfGroup(1, sourcePath),
        )
    }.toList()

private fun screenDeclarations(
    text: String,
    sourcePath: String?,
): List<VariantDeclaration> =
    SCREENS_BLOCK.findAll(text).flatMap { block ->
        val keys = text.blockAfter(block.range.last) ?: return@flatMap emptySequence()

        SCREEN_KEY.findAll(keys.text).map { match ->
            VariantDeclaration(
                name = match.groupValues[1],
                kind = VariantDeclarationKind.SCREEN,
                location =
                    match.locationOfGroup(
                        group = 1,
                        sourcePath = sourcePath,
                        offset = keys.startOffset,
                    ),
            )
        }
    }.toList()

private fun MatchResult.locationOfGroup(
    group: Int,
    sourcePath: String?,
    offset: Int = 0,
): VariantSourceLocation? {
    val range = getRequiredGroup(group).range
    return sourcePath?.let { VariantSourceLocation(it, offset + range.first, offset + range.last + 1) }
}

private fun MatchResult.getRequiredGroup(group: Int): MatchGroup {
    return groups[group] ?: error("regex group $group did not capture a value")
}

private data class TextBlock(
    val text: String,
    val startOffset: Int,
)

private fun String.blockAfter(openingBrace: Int): TextBlock? {
    var depth = 1
    var index = openingBrace + 1
    val limit = (index + MAX_BLOCK_LENGTH).coerceAtMost(length)

    while (index < limit) {
        when (this[index]) {
            '{' -> depth++
            '}' -> {
                depth--
                if (depth == 0) return TextBlock(substring(openingBrace + 1, index), openingBrace + 1)
            }
        }
        index++
    }

    return null
}

private const val MAX_BLOCK_LENGTH = 2_000
