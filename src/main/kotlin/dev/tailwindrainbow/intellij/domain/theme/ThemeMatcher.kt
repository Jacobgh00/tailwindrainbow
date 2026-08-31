package dev.tailwindrainbow.intellij.domain.theme

class ThemeMatcher(private val theme: RainbowTheme) {
    fun matchPrefix(prefix: String): ThemeMatch? = matchPrefixParts(prefix).variant

    /**
     * A whole prefix that the theme names outright wins before anything is stripped, so a
     * variant like `in-range` is never mistaken for `in-` scoping a `range`.
     */
    fun matchPrefixParts(prefix: String): PrefixParts {
        exactMatch(theme.prefix, prefix, SegmentKind.PREFIX)?.let { return PrefixParts(emptyList(), it) }

        val modifiers = modifierSegmentsIn(prefix)
        val scoped = prefix.drop(modifiers.sumOf(ModifierSegment::width))
        val variant = matchScoped(prefix, scoped)

        if (modifiers.none { it.match != null }) return PrefixParts(emptyList(), variant)

        return PrefixParts(modifiers, variant)
    }

    private fun matchScoped(
        prefix: String,
        scoped: String,
    ): ThemeMatch? {
        exactMatch(theme.prefix, scoped, SegmentKind.PREFIX)?.let { return it }

        val unnamed = scoped.substringBefore('/')
        exactMatch(theme.prefix, unnamed, SegmentKind.PREFIX)?.let { return it }
        wildcardMatch(theme.prefix, scoped, SegmentKind.PREFIX)?.let { return it }

        return arbitraryMatch(prefix) ?: arbitraryMatch(scoped) ?: arbitraryMatch(unnamed)
    }

    fun prefixCandidates(prefix: String): PrefixCandidates {
        val scoped = prefix.drop(modifierSegmentsIn(prefix).sumOf(ModifierSegment::width))

        return PrefixCandidates(
            exact = listOf(prefix, scoped, scoped.substringBefore('/')).distinct(),
            cleaned = scoped,
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

    private fun modifierSegmentsIn(prefix: String): List<ModifierSegment> =
        buildList {
            var rest = prefix

            while (true) {
                val name = SCOPING_MODIFIERS.firstOrNull { rest.startsWith(it + DELIMITER) } ?: return@buildList

                add(
                    ModifierSegment(
                        match = exactMatch(theme.prefix, name, SegmentKind.PREFIX),
                        width = name.length + DELIMITER.length,
                    ),
                )
                rest = rest.removePrefix(name + DELIMITER)
            }
        }

    private companion object {
        const val DELIMITER = "-"
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
