package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * Registered after the built-in source, so a spec named `default` retints only the entries it
 * lists and inherits the rest.
 */
class UserThemeSource(private val specs: List<ThemeSpec>) : ThemeSource {
    private val parsed = specs.map(ThemeParser::parse)

    val problems: List<ThemeProblem> = parsed.flatMap { it.problems }

    override fun themes(): Map<String, RainbowTheme> = parsed.associate { it.name to it.theme }
}
