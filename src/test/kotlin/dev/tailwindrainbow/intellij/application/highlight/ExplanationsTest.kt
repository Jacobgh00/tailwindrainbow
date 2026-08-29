package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExplanationsTest {
    private val prefix = segment(start = 0, end = 6, key = "hover")
    private val base = segment(start = 6, end = 17, key = "bg-*")
    private val segments = listOf(prefix, base)

    @Test
    fun `the segment under the caret is the one that explains it`() {
        assertEquals("hover", segmentAt(segments, offset = 0)?.themeKey)
        assertEquals("hover", segmentAt(segments, offset = 5)?.themeKey)
        assertEquals("bg-*", segmentAt(segments, offset = 6)?.themeKey)
    }

    @Test
    fun `a caret resting at the end of the last token still explains it`() {
        assertEquals("bg-*", segmentAt(segments, offset = 17)?.themeKey)
    }

    @Test
    fun `nowhere near a coloured token, nothing is explained`() {
        assertNull(segmentAt(segments, offset = 40))
        assertNull(segmentAt(emptyList(), offset = 0))
    }

    private fun segment(
        start: Int,
        end: Int,
        key: String,
    ) = HighlightSegment(start, end, key, TextStyle("#ffffff", FontWeight.BOLD), SegmentKind.PREFIX)
}
