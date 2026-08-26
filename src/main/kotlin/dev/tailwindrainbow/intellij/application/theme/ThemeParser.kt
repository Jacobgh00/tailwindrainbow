package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

/** A parsed theme plus whatever could not be understood while parsing it. */
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
        if (!HEX_COLOR.matches(color)) return null
        if (fontWeight !in VALID_WEIGHTS) return null
        if (section.needsKey && key.isBlank()) return null

        return TextStyle(color, FontWeight.of(fontWeight), enabled)
    }

    private fun StyleEntry.problem(themeName: String): ThemeProblem {
        val reason = when {
            !HEX_COLOR.matches(color) -> "color must use #RRGGBB format, was '$color'"
            fontWeight !in VALID_WEIGHTS -> "font weight must be a multiple of 100 between 100 and 900, was $fontWeight"
            else -> "a ${section.name.lowercase()} entry needs a key"
        }

        return ThemeProblem(themeName, section, key, reason)
    }

    private val SegmentKind.needsKey: Boolean
        get() = this == SegmentKind.PREFIX || this == SegmentKind.BASE

    private val HEX_COLOR = Regex("^#[0-9a-fA-F]{6}$")
    private val VALID_WEIGHTS = (100..900 step 100).toSet()
}
