package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

internal data class HighlightingSnapshot(
    val settings: HighlightSettings,
    val theme: RainbowTheme,
) {
    val themeName: String get() = settings.themeName
}
