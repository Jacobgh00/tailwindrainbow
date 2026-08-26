package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

/**
 * Everything the IDE adapter is allowed to ask for: no PSI, no editor, no settings service.
 */
fun interface HighlightDocument {
    fun highlight(
        text: String,
        fileExtension: String,
    ): List<HighlightSegment>
}
