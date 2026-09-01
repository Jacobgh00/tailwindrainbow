package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.ThemeMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TailwindClassParserTest {
    private val prefixStyle = TextStyle("#00ff00", FontWeight.BOLD)
    private val responsiveStyle = TextStyle("#ff00ff", FontWeight.BOLD)
    private val baseStyle = TextStyle("#0000ff", FontWeight.NORMAL)
    private val importantStyle = TextStyle("#ff0000", FontWeight.BOLD)
    private val arbitraryStyle = TextStyle("#ffaa00", FontWeight.BOLD)
    private val scopeStyle = TextStyle("#00aaff", FontWeight.BOLD)

    private fun parser(base: Map<String, TextStyle> = emptyMap()) =
        TailwindClassParser(
            ThemeMatcher(
                RainbowTheme(
                    prefix = mapOf("hover" to prefixStyle, "lg" to responsiveStyle),
                    base = base,
                    arbitrary = arbitraryStyle,
                    important = importantStyle,
                ),
            ),
        )

    @Test
    fun `last configured prefix colours the remaining class when base is not configured`() {
        val segments = parser().parse("hover:bg-blue-500", startOffset = 10)

        assertEquals(
            listOf(HighlightSegment(10, 27, 10, 15, "hover", prefixStyle, SegmentKind.PREFIX)),
            segments,
        )
    }

    @Test
    fun `each prefix receives its own non-overlapping segment`() {
        val segments = parser().parse("lg:hover:bg-blue-500")

        assertEquals("lg:", segments[0].sliceOf("lg:hover:bg-blue-500"))
        assertEquals("hover:bg-blue-500", segments[1].sliceOf("lg:hover:bg-blue-500"))
    }

    @Test
    fun `base rule colours the base separately from all prefixes`() {
        val source = "lg:hover:bg-blue-500"
        val segments = parser(mapOf("bg-*" to baseStyle)).parse(source)

        assertEquals(listOf("lg:", "hover:", "bg-blue-500"), segments.map { it.sliceOf(source) })
        assertEquals(listOf("lg", "hover", "bg-*"), segments.map { it.themeKey })
    }

    @Test
    fun `colon inside arbitrary value does not split the base class`() {
        val source = "hover:[mask-type:luminance]"
        val segments = parser().parse(source)

        assertEquals(listOf("hover:", "[mask-type:luminance]"), segments.map { it.sliceOf(source) })
    }

    @Test
    fun `important modifier receives its own segment`() {
        val source = "!hover:bg-blue-500"
        val segments = parser().parse(source)

        assertEquals("!", segments.first().sliceOf(source))
        assertEquals(SegmentKind.IMPORTANT, segments.first().kind)
        assertEquals("hover:bg-blue-500", segments[1].sliceOf(source))
    }

    @Test
    fun `an important modifier written after the class is recognised`() {
        val source = "hover:bg-blue-500!"
        val segments = parser().parse(source)

        assertEquals(listOf("!", "hover:bg-blue-500"), segments.map { it.sliceOf(source) })
        assertEquals(SegmentKind.IMPORTANT, segments.first().kind)
    }

    @Test
    fun `an important modifier written before the utility is recognised`() {
        val source = "hover:!bg-blue-500"
        val segments = parser().parse(source)
        val prefixSegments = segments.filter { it.themeKey == "hover" }

        assertEquals(
            listOf("!", "hover:", "bg-blue-500"),
            segments.map { it.sliceOf(source) },
            "the prefix colours both sides of the marker rather than painting over it",
        )
        assertEquals(SegmentKind.IMPORTANT, segments.first().kind)
        assertEquals(listOf("hover", "hover"), prefixSegments.map { it.matchSliceOf(source) })
    }

    @Test
    fun `an important modifier on a standalone utility is recognised at either end`() {
        val leading = parser(mapOf("bg-*" to baseStyle)).parse("!bg-blue-500")
        val trailing = parser(mapOf("bg-*" to baseStyle)).parse("bg-blue-500!")

        assertEquals(listOf("!", "bg-blue-500"), leading.map { it.sliceOf("!bg-blue-500") })
        assertEquals(listOf("!", "bg-blue-500"), trailing.map { it.sliceOf("bg-blue-500!") })
    }

    @Test
    fun `multiple classes preserve their document offsets`() {
        val source = "flex hover:bg-blue-500 lg:text-xl"
        val segments = parser().parse(source, startOffset = 50)

        assertEquals(listOf(55, 73), segments.map { it.start })
    }

    @Test
    fun `malformed class chains are ignored`() {
        assertTrue(parser().parse(":hover:bg-blue-500 hover::text-white lg:").isEmpty())
    }

    @Test
    fun `a scoped variant paints the modifier apart from the variant it scopes`() {
        val source = "group-hover:bg-blue-500"
        val segments = scopedParser().parse(source)

        assertEquals(listOf("group-", "hover:bg-blue-500"), segments.map { it.sliceOf(source) })
        assertEquals(listOf(scopeStyle, prefixStyle), segments.map(HighlightSegment::style))
    }

    @Test
    fun `a plain variant is still painted as one segment`() {
        val source = "hover:bg-blue-500"

        assertEquals(listOf(source), scopedParser().parse(source).map { it.sliceOf(source) })
    }

    @Test
    fun `stacked modifiers each keep their own segment`() {
        val source = "peer-group-hover:bg-blue-500"
        val segments = scopedParser().parse(source)

        assertEquals(listOf("peer-", "group-", "hover:bg-blue-500"), segments.map { it.sliceOf(source) })
    }

    @Test
    fun `a theme with no colour for the modifier keeps the single segment it had before`() {
        val source = "group-hover:bg-blue-500"
        val segments = parser().parse(source)

        assertEquals(listOf(source), segments.map { it.sliceOf(source) })
        assertEquals("hover", segments.single().matchSliceOf(source))
    }

    @Test
    fun `a scoped variant the theme does not otherwise know still shows its scope`() {
        val source = "group-unknown:bg-blue-500"

        assertEquals(listOf("group-"), scopedParser().parse(source).map { it.sliceOf(source) })
    }

    @Test
    fun `an important marker still splits a scoped variant without overlapping`() {
        val source = "!group-hover:bg-blue-500"
        val segments = scopedParser().parse(source)

        assertEquals(listOf("!", "group-", "hover:bg-blue-500"), segments.map { it.sliceOf(source) })
    }

    @Test
    fun `a variant that merely starts like a modifier keeps its own entry whole`() {
        val source = "in-range:bg-blue-500"

        assertEquals(listOf(source), scopedParser().parse(source).map { it.sliceOf(source) })
    }

    @Test
    fun `a modifier the theme has no colour for still takes up its room`() {
        val source = "peer-group-hover:bg-blue-500"
        val segments = scopedParser(coloured = setOf("group")).parse(source)

        assertEquals(listOf("group-", "hover:bg-blue-500"), segments.map { it.sliceOf(source) })
    }

    private fun scopedParser(coloured: Set<String> = setOf("group", "peer", "in")) =
        TailwindClassParser(
            ThemeMatcher(
                RainbowTheme(
                    prefix =
                        mapOf("hover" to prefixStyle, "lg" to responsiveStyle, "in-range" to responsiveStyle) +
                            coloured.associateWith { scopeStyle },
                    arbitrary = arbitraryStyle,
                    important = importantStyle,
                ),
            ),
        )
}

private fun HighlightSegment.sliceOf(source: String): String = source.substring(start, end)

private fun HighlightSegment.matchSliceOf(source: String): String = source.substring(matchStart, matchEnd)
