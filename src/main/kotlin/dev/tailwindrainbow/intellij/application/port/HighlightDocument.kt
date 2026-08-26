package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

/**
 * Inbound port: the plugin's single use case.
 *
 * Everything the IDE adapter is allowed to ask for. Takes text and a file extension, returns
 * what to paint — no PSI, no editor, no settings service.
 */
fun interface HighlightDocument {
    fun highlight(text: String, fileExtension: String): List<HighlightSegment>
}
