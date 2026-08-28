package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.overriddenBy

class SpecThemeSource(
    specs: List<ThemeSpec>,
    private val bases: ThemeSource = ThemeSource { emptyMap() },
) : ThemeSource {
    private val parsed = specs.map { spec -> spec to ThemeParser.parse(spec) }

    val problems: List<ThemeProblem> = parsed.flatMap { (_, theme) -> theme.problems }

    override fun themes(): Map<String, RainbowTheme> =
        parsed.associate { (spec, parsed) -> spec.name to baseOf(spec).overriddenBy(parsed.theme) }

    private fun baseOf(spec: ThemeSpec): RainbowTheme = bases.themes()[spec.basedOn] ?: RainbowTheme()
}
