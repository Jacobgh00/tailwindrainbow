package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeMergeTest {
    private val builtIn =
        mapOf(
            "default" to
                RainbowTheme(
                    prefix = mapOf("hover" to red, "focus" to red),
                    arbitrary = red,
                    important = red,
                ),
        )

    @Test
    fun `a user override retints one entry and inherits the rest`() {
        val user = mapOf("default" to RainbowTheme(prefix = mapOf("hover" to blue)))

        val theme = merged(builtIn, user).getValue("default")

        assertEquals(blue, theme.prefix["hover"], "the overridden entry")
        assertEquals(red, theme.prefix["focus"], "an entry the user never mentioned")
        assertEquals(red, theme.arbitrary, "a section the user never mentioned")
    }

    @Test
    fun `a user theme with a new name is added alongside the built-ins`() {
        val user = mapOf("midnight" to RainbowTheme(prefix = mapOf("hover" to blue)))

        val themes = merged(builtIn, user)

        assertEquals(setOf("default", "midnight"), themes.keys)
        assertEquals(blue, themes.getValue("midnight").prefix["hover"])
    }

    @Test
    fun `later layers win, so ordering is the override rule`() {
        val first = mapOf("default" to RainbowTheme(prefix = mapOf("hover" to red)))
        val second = mapOf("default" to RainbowTheme(prefix = mapOf("hover" to blue)))

        assertEquals(blue, merged(first, second).getValue("default").prefix["hover"])
        assertEquals(red, merged(second, first).getValue("default").prefix["hover"])
    }

    @Test
    fun `a user theme cannot remove a built-in entry, only restyle it`() {
        val user = mapOf("default" to RainbowTheme(prefix = emptyMap()))

        assertTrue(merged(builtIn, user).getValue("default").prefix.containsKey("hover"))
    }

    private fun merged(vararg layers: Map<String, RainbowTheme>) = mergeThemes(layers.toList())

    private companion object {
        val red = TextStyle("#ff0000", FontWeight.BOLD)
        val blue = TextStyle("#0000ff", FontWeight.BOLD)
    }
}
