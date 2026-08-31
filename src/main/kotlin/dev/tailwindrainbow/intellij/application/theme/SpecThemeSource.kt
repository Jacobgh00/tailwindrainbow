package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.overriddenBy

class SpecThemeSource(
    specs: List<ThemeSpec>,
    private val bases: ThemeSource = ThemeSource { emptyMap() },
) : ThemeSource {
    private val parsed = parseThemeSpecs(specs)

    val problems: List<ThemeProblem> = parsed.flatMap { it.parsed.problems }

    override fun themes(): Map<String, RainbowTheme> = resolveThemeSpecifications(parsed, bases.themes())
}

internal data class ParsedThemeSpec(
    val spec: ThemeSpec,
    val parsed: ParsedTheme,
)

internal fun parseThemeSpecs(specs: List<ThemeSpec>): List<ParsedThemeSpec> =
    specs.map { spec ->
        val copy = spec.copy(entries = spec.entries.toList())
        ParsedThemeSpec(copy, ThemeParser.parse(copy))
    }

internal fun resolveThemeSpecifications(
    specs: Iterable<ParsedThemeSpec>,
    baseThemes: Map<String, RainbowTheme>,
): Map<String, RainbowTheme> =
    specs.associate { parsed ->
        parsed.spec.name to
            (baseThemes[parsed.spec.basedOn] ?: RainbowTheme()).overriddenBy(parsed.parsed.theme)
    }
