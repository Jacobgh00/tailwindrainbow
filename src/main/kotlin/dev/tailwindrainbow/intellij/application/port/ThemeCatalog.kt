package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * Outbound port: resolves a theme name to a palette.
 *
 * The use case does not care that palettes come from built-ins layered with user overrides, nor
 * that the result is cached — only that a name yields a theme.
 */
fun interface ThemeCatalog {
    fun themeNamed(name: String): RainbowTheme
}
