package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.isHexColor

data class ParsedTheme(
    val name: String,
    val theme: RainbowTheme,
    val problems: List<ThemeProblem>,
)

/**
 * Turns an untrusted [ThemeSpec] into a validated [RainbowTheme].
 *
 * Lenient by design: a single malformed color must not cost the user their whole theme, so bad
 * entries are dropped and reported through [ParsedTheme.problems] rather than thrown. This is the
 * boundary that keeps [TextStyle]'s constructor invariant safe to be strict.
 */
object ThemeParser {
    fun parse(spec: ThemeSpec): ParsedTheme {
        val prefix = mutableMapOf<String, TextStyle>()
        val base = mutableMapOf<String, TextStyle>()
        var arbitrary: TextStyle? = null
        var important: TextStyle? = null
        val problems = mutableListOf<ThemeProblem>()

        spec.entries.forEach { entry ->
            val style = entry.toTextStyle()

            if (style == null) {
                problems += entry.problem(spec.name)
                return@forEach
            }

            when (entry.section) {
                SegmentKind.PREFIX -> prefix[entry.key] = style
                SegmentKind.BASE -> base[entry.key] = style
                SegmentKind.ARBITRARY -> arbitrary = style
                SegmentKind.IMPORTANT -> important = style
            }
        }

        return ParsedTheme(
            name = spec.name,
            theme = RainbowTheme(prefix = prefix, base = base, arbitrary = arbitrary, important = important),
            problems = problems,
        )
    }

    private fun StyleEntry.toTextStyle(): TextStyle? {
        if (!color.isHexColor()) return null
        if (!FontWeight.isValid(fontWeight)) return null
        if (section.isKeyed && key.isBlank()) return null

        return TextStyle(color, FontWeight.of(fontWeight), enabled)
    }

    private fun StyleEntry.problem(themeName: String): ThemeProblem {
        val reason =
            when {
                !color.isHexColor() -> "color must use #RRGGBB format, was '$color'"
                !FontWeight.isValid(fontWeight) ->
                    "font weight must be one of ${FontWeight.ALL.sorted()}, was $fontWeight"
                else -> "a ${section.name.lowercase()} entry needs a key"
            }

        return ThemeProblem(themeName, section, key, reason)
    }
}
