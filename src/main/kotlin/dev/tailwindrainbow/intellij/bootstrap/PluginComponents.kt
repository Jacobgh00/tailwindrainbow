package dev.tailwindrainbow.intellij.bootstrap

import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.HighlightDocumentService
import dev.tailwindrainbow.intellij.application.port.HighlightDocument

/**
 * The single place concrete adapters are bound to ports; everything else depends on interfaces,
 * which is what lets the application layer be tested without an IDE.
 */
object PluginComponents {
    fun highlightDocument(): HighlightDocument {
        val settings = TailwindRainbowSettings.getInstance()
        return HighlightDocumentService(settings, settings)
    }
}
