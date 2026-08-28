package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BuiltInThemesTest {
    @Test
    fun `every named theme resolves to a distinct palette`() {
        val palettes = BuiltInThemes.themes().values.toList()

        assertEquals(
            BuiltInThemes.themes().size,
            palettes.distinct().size,
            "two themes share a palette, so switching between them is invisible",
        )
    }

    @Test
    fun `synthwave and default disagree on most shared prefixes`() {
        val default = BuiltInThemes.default
        val synthwave = BuiltInThemes.synthwave
        val shared = default.prefix.keys intersect synthwave.prefix.keys
        val differing = shared.count { default.prefix[it] != synthwave.prefix[it] }

        assertTrue(
            differing > shared.size / 2,
            "only $differing of ${shared.size} shared prefixes differ; the themes look alike",
        )
        assertNotEquals(default.arbitrary, synthwave.arbitrary)
    }

    @Test
    fun `no built-in theme colours base classes`() {
        val withBaseEntries = BuiltInThemes.themes().filterValues { it.base.isNotEmpty() }

        assertTrue(
            withBaseEntries.isEmpty(),
            "${withBaseEntries.keys} colour base classes out of the box; the point of the plugin is " +
                "that variants stand out, which stops being true once every utility is coloured too. " +
                "Base colouring is opt-in, added by the user in the theme editor",
        )
    }

    @Test
    fun `an unknown theme name falls back to default`() {
        assertEquals(BuiltInThemes.default, ThemeRepository(BuiltInThemes).find("no-such-theme"))
    }
}
