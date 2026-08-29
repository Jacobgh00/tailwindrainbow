package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PaintedFileHarnessTest : PaintedFileTest() {
    @Test
    fun `the harness reports what the plugin painted, and where`() {
        val painted = painted("page.html", """<div class="hover:bg-blue-500 lg:text-xl"></div>""")

        assertEquals(listOf("hover:bg-blue-500", "lg:text-xl"), painted.map { it.text })
    }

    @Test
    fun `a colour is adapted to the editor background before it is painted`() {
        val painted = painted("adapted.html", """<div class="hover:bg-blue-500"></div>""")

        assertTrue(painted.single().color.isNotEmpty(), "a colour was applied")
        assertTrue(
            painted.single().color != "#4ee585",
            "the theme's colour is unreadable on this background, so the painted one differs",
        )
    }
}
