package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeRepositoryTest {
    private val builtIn = ThemeSource {
        mapOf(
            "default" to RainbowTheme(
                prefix = mapOf("hover" to red, "focus" to red),
                arbitrary = red,
                important = red,
            ),
        )
    }

    @Test
    fun `a user override retints one entry and inherits the rest`() {
        val user = ThemeSource { mapOf("default" to RainbowTheme(prefix = mapOf("hover" to blue))) }

        val theme = ThemeRepository(builtIn, user).find("default")

        assertEquals(blue, theme.prefix["hover"], "the overridden entry")
        assertEquals(red, theme.prefix["focus"], "an entry the user never mentioned")
        assertEquals(red, theme.arbitrary, "a section the user never mentioned")
    }

    @Test
    fun `a user theme with a new name is added alongside the built-ins`() {
        val user = ThemeSource { mapOf("midnight" to RainbowTheme(prefix = mapOf("hover" to blue))) }

        val repository = ThemeRepository(builtIn, user)

        assertEquals(setOf("default", "midnight"), repository.names)
        assertEquals(blue, repository.find("midnight").prefix["hover"])
    }

    @Test
    fun `later sources win, so ordering is the override rule`() {
        val first = ThemeSource { mapOf("default" to RainbowTheme(prefix = mapOf("hover" to red))) }
        val second = ThemeSource { mapOf("default" to RainbowTheme(prefix = mapOf("hover" to blue))) }

        assertEquals(blue, ThemeRepository(first, second).find("default").prefix["hover"])
        assertEquals(red, ThemeRepository(second, first).find("default").prefix["hover"])
    }

    @Test
    fun `an unknown name falls back to the default theme`() {
        assertEquals(ThemeRepository(builtIn).find("default"), ThemeRepository(builtIn).find("nope"))
    }

    @Test
    fun `a user theme cannot remove a built-in entry, only restyle it`() {
        val user = ThemeSource { mapOf("default" to RainbowTheme(prefix = emptyMap())) }

        assertTrue(ThemeRepository(builtIn, user).find("default").prefix.containsKey("hover"))
    }

    private companion object {
        val red = TextStyle("#ff0000", FontWeight.BOLD)
        val blue = TextStyle("#0000ff", FontWeight.BOLD)
    }
}

