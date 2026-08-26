package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings

/**
 * Outbound port: what the highlighter needs to know about user preferences.
 *
 * Exists so the use case never reaches for a global service. The IntelliJ state component
 * implements it; tests pass a literal.
 */
fun interface SettingsProvider {
    fun current(): HighlightSettings
}

data class HighlightSettings(
    val enabled: Boolean,
    val themeName: String,
    val scan: ScanSettings,
)
