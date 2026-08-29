package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeFileTest {
    private val palette =
        RainbowTheme(
            prefix = mapOf("hover" to TextStyle("#4ee585", FontWeight.BOLD)),
            base = mapOf("bg-*" to TextStyle("#0000ff", FontWeight.NORMAL, enabled = false)),
            arbitrary = TextStyle("#ff9987", FontWeight.BOLD),
            important = TextStyle("#ff0000", FontWeight.BOLD),
        )

    @Test
    fun `a palette survives being written out and read back`() {
        val read = themesFromFile(palette.toThemeFile("midnight")).singleOrNull()

        assertEquals("midnight", read?.name)
        assertEquals(
            listOf(
                StyleEntry(SegmentKind.PREFIX, "hover", "#4ee585", 700),
                StyleEntry(SegmentKind.BASE, "bg-*", "#0000ff", 400, enabled = false),
                StyleEntry(SegmentKind.ARBITRARY, "", "#ff9987", 700),
                StyleEntry(SegmentKind.IMPORTANT, "", "#ff0000", 700),
            ),
            read?.entries,
        )
    }

    @Test
    fun `what is written is what the VS Code extension writes`() {
        val written = palette.toThemeFile("midnight")

        listOf("\"midnight\"", "\"prefix\"", "\"hover\"", "\"color\"", "\"#4ee585\"", "\"fontWeight\"")
            .forEach { assertTrue(it in written, "expected $it in\n$written") }
    }

    @Test
    fun `a theme exported from the VS Code extension reads`() {
        val theirs =
            """
            {
              "myTheme": {
                "prefix": {
                  "hover": { "color": "#ff0000", "fontWeight": "bold" },
                  "sm": { "color": "#00ff00" }
                },
                "base": { "bg-*": { "color": "#ff6600", "fontWeight": "semibold" } },
                "arbitrary": { "color": "#ff00ff", "fontWeight": "700" },
                "important": { "color": "#ff0000", "fontWeight": "bold" }
              }
            }
            """.trimIndent()

        val read = checkNotNull(themesFromFile(theirs).singleOrNull())

        assertEquals("myTheme", read.name)
        assertEquals(700, read.entries.first { it.key == "hover" }.fontWeight, "named weights are read")
        assertEquals(400, read.entries.first { it.key == "sm" }.fontWeight, "a missing weight is not bold")
        assertEquals(600, read.entries.first { it.key == "bg-*" }.fontWeight)
    }

    @Test
    fun `a file that is not a theme is refused rather than throwing`() {
        assertTrue(themesFromFile("not json at all").isEmpty())
        assertTrue(themesFromFile("[]").isEmpty())
        assertTrue(themesFromFile("{}").isEmpty())
    }

    @Test
    fun `an entry the plugin cannot use is reported, not thrown, exactly as a stored one is`() {
        val broken = """{ "mine": { "prefix": { "hover": { "color": "rebeccapurple" } } } }"""

        val spec = checkNotNull(themesFromFile(broken).singleOrNull())
        val problems = SpecThemeSource(listOf(spec)).problems

        assertEquals(1, problems.size)
        assertTrue(problems.single().message.contains("#RRGGBB"), problems.single().message)
    }
}
