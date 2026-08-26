package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ThemeParserTest {
    @Test
    fun `a valid entry becomes a styled prefix`() {
        val parsed = ThemeParser.parse(spec(entry(color = "#a1b2c3", fontWeight = 700)))

        assertEquals("#a1b2c3", parsed.theme.prefix["hover"]?.color)
        assertEquals(700, parsed.theme.prefix["hover"]?.fontWeight?.value)
        assertTrue(parsed.problems.isEmpty())
    }

    @Test
    fun `a malformed colour is reported instead of thrown`() {
        val parsed = ThemeParser.parse(spec(entry(color = "#GGGGGG")))

        assertNull(parsed.theme.prefix["hover"])
        assertEquals(1, parsed.problems.size)
        assertTrue(parsed.problems.single().message.contains("#RRGGBB"))
    }

    @Test
    fun `one bad entry does not cost the user the rest of the theme`() {
        val parsed =
            ThemeParser.parse(
                ThemeSpec(
                    "mine",
                    listOf(
                        entry(key = "hover", color = "not-a-colour"),
                        entry(key = "focus", color = "#00ff00"),
                    ),
                ),
            )

        assertNull(parsed.theme.prefix["hover"])
        assertEquals("#00ff00", parsed.theme.prefix["focus"]?.color)
        assertEquals(1, parsed.problems.size)
    }

    @Test
    fun `an out of range font weight is reported`() {
        val parsed = ThemeParser.parse(spec(entry(fontWeight = 650)))

        assertTrue(parsed.problems.single().message.contains("font weight"))
        assertTrue(parsed.problems.single().message.contains("650"))
    }

    @Test
    fun `arbitrary and important need no key`() {
        val parsed =
            ThemeParser.parse(
                ThemeSpec(
                    "mine",
                    listOf(
                        StyleEntry(SegmentKind.ARBITRARY, key = "", color = "#111111", fontWeight = 700),
                        StyleEntry(SegmentKind.IMPORTANT, key = "", color = "#222222", fontWeight = 900),
                    ),
                ),
            )

        assertEquals("#111111", parsed.theme.arbitrary?.color)
        assertEquals("#222222", parsed.theme.important?.color)
        assertTrue(parsed.problems.isEmpty())
    }

    @Test
    fun `a prefix entry without a key is reported`() {
        val parsed = ThemeParser.parse(spec(entry(key = "")))

        assertTrue(parsed.problems.single().message.contains("needs a key"))
    }

    private fun spec(vararg entries: StyleEntry) = ThemeSpec("mine", entries.toList())

    private fun entry(
        key: String = "hover",
        color: String = "#123456",
        fontWeight: Int = 700,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, fontWeight)
}
