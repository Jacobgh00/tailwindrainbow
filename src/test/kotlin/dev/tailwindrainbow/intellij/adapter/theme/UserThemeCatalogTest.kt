package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserThemeCatalogTest {
    private val contributedStyle = TextStyle("#010101", FontWeight.BOLD)
    private val contributed =
        ThemeSource { mapOf("corporate" to RainbowTheme(prefix = mapOf("hover" to contributedStyle))) }

    @Test
    fun `a contributed theme is offered alongside the built-ins`() {
        val catalog = UserThemeCatalog(contributed)

        assertTrue(catalog.names().containsAll(BuiltInThemes.themes().keys + "corporate"))
    }

    @Test
    fun `the user's own colour wins over the one that was contributed`() {
        val catalog = UserThemeCatalog(contributed)

        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("hover", "#020202")))))

        assertEquals("#020202", catalog.themeNamed("corporate").prefix.getValue("hover").color)
    }

    @Test
    fun `an untouched entry of a contributed theme keeps its contributed colour`() {
        val catalog = UserThemeCatalog(contributed)

        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("focus", "#020202")))))

        assertEquals(contributedStyle, catalog.themeNamed("corporate").prefix["hover"])
    }

    @Test
    fun `the palette a row falls back to includes contributed themes`() {
        val catalog = UserThemeCatalog(contributed)
        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("hover", "#020202")))))

        assertEquals(
            contributedStyle,
            catalog.basePalette("corporate").prefix["hover"],
            "resetting the row must offer the contributed colour, not the built-in one",
        )
    }

    @Test
    fun `a contributed theme cannot be deleted, so it counts as a base`() {
        assertTrue("corporate" in UserThemeCatalog(contributed).baseNames())
    }

    @Test
    fun `refresh copies the caller's theme list`() {
        val themes = mutableListOf(ThemeSpec("local", listOf(entry("hover", "#020202"))))
        val catalog = UserThemeCatalog()

        catalog.refresh(themes)
        themes.clear()

        assertEquals("local", catalog.overrides().single().name)
        assertTrue("local" in catalog.names())
    }

    @Test
    fun `refresh copies the entries inside the caller's theme list`() {
        val entries = mutableListOf(entry("hover", "#020202"))
        val catalog = UserThemeCatalog()

        catalog.refresh(listOf(ThemeSpec("local", entries)))
        entries.clear()

        assertEquals(1, catalog.overrides().single().entries.size)
    }

    @Test
    fun `a previously published view remains from its original generation`() {
        val catalog = UserThemeCatalog()
        val first = ThemeSpec("first", listOf(entry("hover", "#020202")))
        val second = ThemeSpec("second", listOf(entry("hover", "#030303")))

        catalog.refresh(listOf(first))
        val firstOverrides = catalog.overrides()

        catalog.refresh(listOf(second))

        assertEquals(listOf(first), firstOverrides)
        assertEquals(listOf(second), catalog.overrides())
        assertTrue("second" in catalog.names())
        assertTrue("first" !in catalog.names())
    }

    private fun entry(
        key: String,
        color: String,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, FontWeight.BOLD.value)
}
