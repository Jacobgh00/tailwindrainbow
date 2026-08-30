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

object ThemeParser {
    fun parse(spec: ThemeSpec): ParsedTheme {
        val prefix = mutableMapOf<String, TextStyle>()
        val base = mutableMapOf<String, TextStyle>()
        var arbitrary: TextStyle? = null
        var important: TextStyle? = null
        val problems = mutableListOf<ThemeProblem>()

        spec.entries.forEach { entry ->
            when (val result = entry.parse()) {
                is EntryResult.Valid ->
                    when (entry.section) {
                        SegmentKind.PREFIX -> prefix[entry.key] = result.style
                        SegmentKind.BASE -> base[entry.key] = result.style
                        SegmentKind.ARBITRARY -> arbitrary = result.style
                        SegmentKind.IMPORTANT -> important = result.style
                    }
                is EntryResult.Invalid -> problems += ThemeProblem(spec.name, entry.section, entry.key, result.message)
            }
        }

        return ParsedTheme(
            name = spec.name,
            theme = RainbowTheme(prefix = prefix, base = base, arbitrary = arbitrary, important = important),
            problems = problems,
        )
    }

    private fun StyleEntry.parse(): EntryResult {
        if (!color.isHexColor()) {
            return EntryResult.Invalid("color must use #RRGGBB format, was '$color'")
        }

        if (!FontWeight.isValid(fontWeight)) {
            return EntryResult.Invalid("font weight must be one of ${FontWeight.ALL.sorted()}, was $fontWeight")
        }

        if (section.isKeyed && key.isBlank()) {
            return EntryResult.Invalid("a ${section.name.lowercase()} entry needs a key")
        }

        return EntryResult.Valid(TextStyle(color, FontWeight.of(fontWeight), enabled))
    }

    private sealed interface EntryResult {
        data class Valid(val style: TextStyle) : EntryResult

        data class Invalid(val message: String) : EntryResult
    }
}
