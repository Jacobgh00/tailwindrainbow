package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

fun interface HighlightDocument {
    fun highlight(
        text: String,
        fileExtension: String,
    ): List<HighlightSegment>
}
