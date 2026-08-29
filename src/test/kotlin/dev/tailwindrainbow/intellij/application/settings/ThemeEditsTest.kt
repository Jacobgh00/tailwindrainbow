package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
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
}
