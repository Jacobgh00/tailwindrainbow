package dev.tailwindrainbow.intellij.domain.theme

class ThemeMatcher(private val theme: RainbowTheme) {
    fun matchPrefix(prefix: String): ThemeMatch? {
        exactMatch(theme.prefix, prefix, SegmentKind.PREFIX)?.let {
            return it
        }

        val cleanedPrefix = removeIgnoredModifiers(prefix)
        exactMatch(theme.prefix, cleanedPrefix, SegmentKind.PREFIX)?.let {
            return it
        }

        val unnamedPrefix = cleanedPrefix.substringBefore('/')
        exactMatch(theme.prefix, unnamedPrefix, SegmentKind.PREFIX)?.let {
            return it
        }

        wildcardMatch(theme.prefix, cleanedPrefix, SegmentKind.PREFIX)?.let {
            return it
        }

        return arbitraryMatch(prefix) ?: arbitraryMatch(cleanedPrefix) ?: arbitraryMatch(unnamedPrefix)
    }

    fun prefixCandidates(prefix: String): PrefixCandidates {
        val cleanedPrefix = removeIgnoredModifiers(prefix)

        return PrefixCandidates(
            exact = listOf(prefix, cleanedPrefix, cleanedPrefix.substringBefore('/')).distinct(),
            cleaned = cleanedPrefix,
        )
    }

    fun matchBase(className: String): ThemeMatch? {
        exactMatch(theme.base, className, SegmentKind.BASE)?.let {
            return it
        }

        wildcardMatch(theme.base, className, SegmentKind.BASE)?.let {
            return it
        }

        return arbitraryMatch(className)
    }

    fun matchImportant(): ThemeMatch? = theme.important.toMatch(IMPORTANT_KEY, SegmentKind.IMPORTANT)

    private fun arbitraryMatch(value: String): ThemeMatch? =
        if (value.isArbitrary()) theme.arbitrary.toMatch(ARBITRARY_KEY, SegmentKind.ARBITRARY) else null

    private fun exactMatch(
        styles: Map<String, TextStyle>,
        value: String,
        kind: SegmentKind,
    ): ThemeMatch? = styles[value].toMatch(value, kind)

    private fun wildcardMatch(
        styles: Map<String, TextStyle>,
        value: String,
        kind: SegmentKind,
    ): ThemeMatch? =
        styles.entries
            .filter { (pattern, style) -> style.enabled && pattern.isWildcard() && pattern.matchesGlob(value) }
            .maxByOrNull { (pattern) -> pattern.count { it != '*' } }
            ?.let { (pattern, style) -> ThemeMatch(pattern, style, kind) }

    private fun removeIgnoredModifiers(prefix: String): String {
        var result = prefix

        while (true) {
            val modifier = SCOPING_MODIFIERS.firstOrNull { result.startsWith("$it-") } ?: return result
            result = result.removePrefix("$modifier-")
        }
    }

    private companion object {
        const val ARBITRARY_KEY = "arbitrary"
        const val IMPORTANT_KEY = "important"
    }
}

fun Iterable<String>.wildcardsCovering(value: String): List<String> =
    filter { pattern -> pattern.isWildcard() && pattern.matchesGlob(value) }
        .sortedByDescending { pattern -> pattern.count { it != '*' } }

private fun TextStyle?.toMatch(
    key: String,
    kind: SegmentKind,
): ThemeMatch? = this?.takeIf(TextStyle::enabled)?.let { ThemeMatch(key, it, kind) }

private fun String.isArbitrary(): Boolean = startsWith('[') && endsWith(']')

private val LITERAL_STAR_VARIANTS = setOf("*", "**")

private fun String.isWildcard(): Boolean = '*' in this && this !in LITERAL_STAR_VARIANTS

private fun String.matchesGlob(value: String): Boolean {
    val parts = split('*')
    var offset = 0

    if (!startsWith('*') && !value.startsWith(parts.first())) {
        return false
    }

    for (part in parts.filter(String::isNotEmpty)) {
        val index = value.indexOf(part, offset)
        if (index < 0) return false
        offset = index + part.length
    }

    return endsWith('*') || value.endsWith(parts.last())
}
