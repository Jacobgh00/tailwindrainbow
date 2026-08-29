package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ThemeWorkspaceTest {
    private val hover = StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700)
    private val focus = StyleEntry(SegmentKind.PREFIX, "focus", "#123456", 700)

    private val mine = ThemeSpec("mine", listOf(hover), basedOn = "default")
    private val editingMine = ThemeWorkspace(listOf(mine), editing = "mine")

    @Test
    fun `what the editor holds replaces what was stored for the theme being edited`() {
        val edited = ThemeSpec("mine", listOf(hover, focus), basedOn = "default")

        assertEquals(listOf(edited), editingMine.holding(edited).themes)
    }

    @Test
    fun `a theme the editor no longer has anything to say about is dropped`() {
        assertEquals(emptyList(), editingMine.holding(null).themes)
    }

    @Test
    fun `the editor is ignored when nothing is being edited`() {
        val stale = ThemeSpec("mine", listOf(hover, focus), basedOn = "default")

        assertEquals(listOf(mine), ThemeWorkspace(listOf(mine)).holding(stale).themes)
    }

    @Test
    fun `selecting names the theme the editor is now showing`() {
        assertEquals("mine", ThemeWorkspace(listOf(mine)).holding(null).selecting("mine").editing)
    }

    @Test
    fun `a new theme is added on the base it was given, with nothing being edited`() {
        val created = editingMine.holding(mine).creating("yours", basedOn = "synthwave")

        assertEquals(ThemeSpec("yours", emptyList(), basedOn = "synthwave"), created.themes.last())
        assertEquals("", created.editing, "the editor is about to be reloaded, so it holds nothing")
    }

    @Test
    fun `duplicating and renaming go through the same door as everything else`() {
        assertEquals("copy", editingMine.holding(mine).duplicating("mine", "copy").themes.last().name)
        assertEquals("yours", editingMine.holding(mine).renaming("mine", "yours").themes.single().name)
    }

    @Test
    fun `removing a theme drops it and leaves nothing being edited`() {
        val removed = editingMine.holding(mine).removing("mine")

        assertEquals(emptyList(), removed.themes)
        assertEquals("", removed.editing)
    }

    @Test
    fun `imported themes replace the ones they share a name with`() {
        val imported = ThemeSpec("mine", listOf(focus), basedOn = "synthwave")

        assertEquals(listOf(imported), editingMine.holding(mine).merging(listOf(imported)).themes)
    }

    @Test
    fun `entries the plugin cannot use are dropped from the themes that hold them`() {
        val problem = ThemeProblem("mine", SegmentKind.PREFIX, "hover", "colour is wrong")

        val cleaned = editingMine.holding(mine).withoutEntriesFor(listOf(problem))

        assertEquals(emptyList(), cleaned.themes.single().entries)
    }

    @Test
    fun `a theme's base is its own name until it says otherwise`() {
        assertEquals("default", ThemeWorkspace(listOf(mine)).baseOf("mine"))
        assertEquals("synthwave", ThemeWorkspace(listOf(mine)).baseOf("synthwave"))
    }

    @Test
    fun `a name the workspace does not hold has no spec`() {
        assertEquals(mine, ThemeWorkspace(listOf(mine)).specFor("mine"))
        assertNull(ThemeWorkspace(listOf(mine)).specFor("synthwave"))
    }
}
