package dev.tailwindrainbow.intellij.domain

class ThemeMatcher(
    private val theme: RainbowTheme,
    private val ignoredPrefixModifiers: Set<String>
) {
    fun matchPrefix(prefix: String): ThemeMatch? {
        exactMatch(theme.prefix, prefix)?.let { return it }

        val cleanedPrefix = removeIgnoredModifiers(prefix)
        exactMatch(theme.prefix, cleanedPrefix)?.let { return it }

        val unnamedPrefix = cleanedPrefix.substringBefore('/')
        exactMatch(theme.prefix, unnamedPrefix)?.let { return it }

        wildcardMatch(theme.prefix, cleanedPrefix)?.let { return it }
        return arbitraryMatch(prefix)
    }

    fun matchBase(className: String): ThemeMatch? {
        exactMatch(theme.base, className)?.let { return it }
        wildcardMatch(theme.base, className)?.let { return it }

        return if (className.isArbitrary()) {
            theme.arbitrary?.takeIf(TextStyle::enabled)?.let { ThemeMatch("arbitrary", it) }
        } else {
            null
        }
    }

    fun matchImportant(): ThemeMatch? =
        theme.important?.takeIf(TextStyle::enabled)?.let { ThemeMatch("important", it) }

    private fun arbitraryMatch(value: String): ThemeMatch? =
        if (value.isArbitrary()) {
            theme.arbitrary?.takeIf(TextStyle::enabled)?.let { ThemeMatch("arbitrary", it) }
        } else {
            null
        }

    private fun removeIgnoredModifiers(prefix: String): String {
        var result = prefix

        while (true) {
            val modifier = ignoredPrefixModifiers.firstOrNull { result.startsWith("$it-") } ?: return result
            result = result.removePrefix("$modifier-")
        }
    }

    private fun exactMatch(styles: Map<String, TextStyle>, value: String): ThemeMatch? =
        styles[value]?.takeIf(TextStyle::enabled)?.let { ThemeMatch(value, it) }

    private fun wildcardMatch(styles: Map<String, TextStyle>, value: String): ThemeMatch? =
        styles.entries
            .asSequence()
            .filter { (pattern, style) ->
                style.enabled && pattern !in LITERAL_STAR_VARIANTS && '*' in pattern && pattern.matchesGlob(value)
            }
            .maxByOrNull { (pattern) -> pattern.count { it != '*' } }
            ?.let { (pattern, style) -> ThemeMatch(pattern, style) }

    private companion object {
        val LITERAL_STAR_VARIANTS = setOf("*", "**")
    }

    private fun String.isArbitrary(): Boolean = startsWith('[') && endsWith(']')

    private fun String.matchesGlob(value: String): Boolean {
        val parts = split('*')
        var offset = 0

        if (!startsWith('*') && !value.startsWith(parts.first())) return false

        for (part in parts.filter(String::isNotEmpty)) {
            val index = value.indexOf(part, offset)
            if (index < 0) return false
            offset = index + part.length
        }

        return endsWith('*') || value.endsWith(parts.last())
    }
}