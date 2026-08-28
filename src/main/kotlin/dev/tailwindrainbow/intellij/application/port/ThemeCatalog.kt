package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

fun interface ThemeCatalog {
    fun themeNamed(name: String): RainbowTheme
}
