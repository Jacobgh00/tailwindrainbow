package dev.tailwindrainbow.intellij.domain.highlight

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

data class HighlightSegment(
    val start: Int,
    val end: Int,
    val themeKey: String,
    val style: TextStyle,
    val kind: SegmentKind,
) {
    init {
        require(start >= 0) { "Segment start must not be negative" }
        require(end > start) { "Segment end must be greater than start" }
    }
}
