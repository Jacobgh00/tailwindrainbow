package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings

fun interface SettingsProvider {
    fun current(): HighlightSettings
}

data class HighlightSettings(
    val enabled: Boolean,
    val themeName: String,
    val scan: ScanSettings,
)
