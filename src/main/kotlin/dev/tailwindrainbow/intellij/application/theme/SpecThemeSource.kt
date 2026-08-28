package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.overriddenBy

/**
 * Turns themes described as specs into palettes, whoever wrote them — the user in settings, or
 * another plugin through the contribution point.
 *
 * Each spec is its [ThemeSpec.basedOn] palette with the user's entries on top, which covers both
 * cases in one rule: restyling an existing theme names itself as its base, and a theme the user
 * created names the one it was started from. [bases] is where those base palettes come from —
 * without it a spec resolves to the user's entries alone.
 */
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
