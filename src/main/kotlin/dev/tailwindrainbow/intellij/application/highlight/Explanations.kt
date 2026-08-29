package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

fun segmentAt(
    segments: List<HighlightSegment>,
    offset: Int,
): HighlightSegment? =
    segments.firstOrNull { offset in it.start until it.end }
        ?: segments.firstOrNull { offset == it.end }
