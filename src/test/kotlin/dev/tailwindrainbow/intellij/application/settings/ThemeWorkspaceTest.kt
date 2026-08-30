package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThemeWorkspaceTest {
    private val first = ThemeSpec("first", listOf(entry("hover", "#111111")), basedOn = "default")
    private val second = ThemeSpec("second", listOf(entry("focus", "#222222")), basedOn = "synthwave")

    @Test
    fun `loading themes starts with no draft and finds their bases`() {
        val workspace = ThemeWorkspace.load(listOf(first, second))

        assertNull(workspace.editing)
        assertEquals("default", workspace.baseOf("first"))
        assertEquals(first, workspace.themes.first())
    }

    @Test
    fun `selecting a theme commits the previous draft before switching`() {
        val edited = first.copy(entries = listOf(entry("hover", "#abcdef")))
        val workspace =
            ThemeWorkspace
                .load(listOf(first, second))
                .select("first", null)
                .select("second", edited)

        assertEquals("second", workspace.editing)
        assertEquals(edited, workspace.themes.first { it.name == "first" })
        assertEquals(second, workspace.selectedTheme())
    }

    @Test
    fun `committing a missing draft removes the selected theme`() {
        val workspace =
            ThemeWorkspace
                .load(listOf(first, second))
                .select("first", null)
                .commit(null)

        assertEquals(listOf(second), workspace.themes)
    }

    @Test
    fun `a fresh load discards the previous draft and selection`() {
        val edited = first.copy(entries = listOf(entry("hover", "#abcdef")))
        val workspace =
            ThemeWorkspace
                .load(listOf(first))
                .select("first", null)
                .select("second", edited)
                .let { ThemeWorkspace.load(listOf(second)) }

        assertNull(workspace.editing)
        assertEquals(listOf(second), workspace.themes)
    }

    private fun entry(
        key: String,
        color: String,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, 700)
}
