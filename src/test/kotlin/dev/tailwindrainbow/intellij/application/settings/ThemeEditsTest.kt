package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals

class ThemeEditsTest {
    private val hover = StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700)

    private val mine = ThemeSpec("mine", listOf(hover), basedOn = "default")
    private val onTopOfMine = ThemeSpec("on-top", emptyList(), basedOn = "mine")

    @Test
    fun `duplicating a theme copies its colours and its base under the new name`() {
        val duplicated = listOf(mine).duplicating("mine", "copy")

        assertEquals(
            listOf(mine, ThemeSpec("copy", listOf(hover), basedOn = "default")),
            duplicated,
        )
    }

    @Test
    fun `duplicating a built-in theme gives a theme that follows it, since it has no overrides to copy`() {
        val duplicated = emptyList<ThemeSpec>().duplicating("synthwave", "mine")

        assertEquals(listOf(ThemeSpec("mine", emptyList(), basedOn = "synthwave")), duplicated)
    }

    @Test
    fun `renaming a theme renames it`() {
        val renamed = listOf(mine).renaming("mine", "yours")

        assertEquals(listOf(ThemeSpec("yours", listOf(hover), basedOn = "default")), renamed)
    }

    @Test
    fun `renaming a theme carries the themes that inherit from it`() {
        val renamed = listOf(mine, onTopOfMine).renaming("mine", "yours")

        assertEquals("yours", renamed.single { it.name == "on-top" }.basedOn)
    }

    @Test
    fun `renaming a theme based on itself keeps it based on itself`() {
        val standalone = ThemeSpec("mine", listOf(hover))

        val renamed = listOf(standalone).renaming("mine", "yours")

        assertEquals(ThemeSpec("yours", listOf(hover), basedOn = "yours"), renamed.single())
    }

    @Test
    fun `renaming something the list does not hold leaves it alone`() {
        assertEquals(listOf(mine), listOf(mine).renaming("synthwave", "yours"))
    }

    @Test
    fun `a new entry has the editor's default colour and weight`() {
        assertEquals(
            StyleEntry(SegmentKind.PREFIX, "focus", ADDED_TOKEN_COLOR, FontWeight.BOLD.value),
            newThemeEntry(SegmentKind.PREFIX, "focus"),
        )
    }

    @Test
    fun `adding an entry creates a same-name theme when none exists`() {
        val entry = newThemeEntry(SegmentKind.PREFIX, "hover")

        assertEquals(
            listOf(ThemeSpec("mine", listOf(entry))),
            emptyList<ThemeSpec>().addingEntry("mine", entry),
        )
    }

    @Test
    fun `adding an entry preserves the existing base`() {
        val existing = ThemeSpec("mine", listOf(hover), basedOn = "synthwave")
        val entry = newThemeEntry(SegmentKind.PREFIX, "focus")

        val updated = listOf(existing).addingEntry("mine", entry).single()

        assertEquals("synthwave", updated.basedOn)
        assertEquals(listOf(hover, entry), updated.entries)
    }

    @Test
    fun `adding an entry replaces disabled and duplicate copies of the same key`() {
        val disabled = StyleEntry(SegmentKind.PREFIX, "hover", "#111111", 700, enabled = false)
        val other = StyleEntry(SegmentKind.BASE, "bg-*", "#222222", 400)
        val replacement = newThemeEntry(SegmentKind.PREFIX, "hover")
        val existing = ThemeSpec("mine", listOf(disabled, other, hover))

        val updated = listOf(existing).addingEntry("mine", replacement).single()

        assertEquals(listOf(other, replacement), updated.entries)
    }
}
