package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class SpecThemeSourceTest {
    private val builtIns =
        ThemeSource {
            mapOf(
                "default" to RainbowTheme(prefix = mapOf("hover" to red), arbitrary = red),
                "synthwave" to RainbowTheme(prefix = mapOf("hover" to blue, "focus" to blue)),
            )
        }

    @Test
    fun `a theme based on another starts from that palette`() {
        val spec = ThemeSpec("midnight", listOf(entry("focus", "#0a0a0a")), basedOn = "synthwave")

        val theme = SpecThemeSource(listOf(spec), builtIns).themes().getValue("midnight")

        assertEquals(blue, theme.prefix["hover"], "an entry the user never touched, inherited from the base")
        assertEquals("#0a0a0a", theme.prefix.getValue("focus").color, "the user's own entry")
    }

    @Test
    fun `a spec that restyles an existing theme is based on that theme`() {
        val spec = ThemeSpec("default", listOf(entry("hover", "#0a0a0a")))

        val theme = SpecThemeSource(listOf(spec), builtIns).themes().getValue("default")

        assertEquals("#0a0a0a", theme.prefix.getValue("hover").color)
        assertEquals(red, theme.arbitrary, "the rest of the base palette is still there")
    }

    @Test
    fun `a base that no longer exists leaves the theme with the user's entries alone`() {
        val spec = ThemeSpec("orphan", listOf(entry("hover", "#0a0a0a")), basedOn = "deleted-theme")

        val theme = SpecThemeSource(listOf(spec), builtIns).themes().getValue("orphan")

        assertEquals(mapOf("hover" to TextStyle("#0a0a0a", FontWeight.BOLD)), theme.prefix)
    }

    @Test
    fun `a malformed entry is reported and dropped, not thrown`() {
        val spec = ThemeSpec("midnight", listOf(entry("focus", "not-a-colour")), basedOn = "synthwave")

        val source = SpecThemeSource(listOf(spec), builtIns)

        assertEquals(1, source.problems.size)
        assertEquals(blue, source.themes().getValue("midnight").prefix["focus"], "the base colour survives")
    }

    private fun entry(
        key: String,
        color: String,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, FontWeight.BOLD.value)

    private companion object {
        val red = TextStyle("#ff0000", FontWeight.BOLD)
        val blue = TextStyle("#0000ff", FontWeight.BOLD)
    }
}
