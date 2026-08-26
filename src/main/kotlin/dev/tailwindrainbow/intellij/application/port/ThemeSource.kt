package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

fun interface ThemeSource {
    fun themes(): Map<String, RainbowTheme>
}
