package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.highlight.TailwindClassParser
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

    private fun parser(base: Map<String, TextStyle> = emptyMap()) = TailwindClassParser(
        ThemeMatcher(
            RainbowTheme(
                prefix = mapOf("hover" to prefixStyle, "lg" to responsiveStyle),
                base = base,
                arbitrary = arbitraryStyle,
                important = importantStyle,
            ),
            ignoredPrefixModifiers = setOf("group", "peer"),
        ),
    )

    @Test
    fun `last configured prefix colours the remaining class when base is not configured`() {
        val segments = parser().parse("hover:bg-blue-500", startOffset = 10)

        assertEquals(
            listOf(HighlightSegment(10, 27, "hover", prefixStyle, SegmentKind.PREFIX)),
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
}

private fun HighlightSegment.sliceOf(source: String): String = source.substring(start, end)

