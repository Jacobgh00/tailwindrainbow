package dev.tailwindrainbow.intellij.domain.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ShadesTest {
    @Test
    fun `lightening moves a colour towards white and darkening towards black`() {
        val base = "#4ee585"

        assertTrue(base.lightened(0.2).isHexColor())
        assertNotEquals(base, base.lightened(0.2))
        assertNotEquals(base.lightened(0.2), base.lightened(-0.2))
    }

    @Test
    fun `a ramp of shades keeps every step a colour of its own`() {
        val shades = "#0072b2".shades(5)

        assertEquals(5, shades.size)
        assertEquals(shades.size, shades.distinct().size, "two steps are the same colour: $shades")
        assertTrue(shades.all(String::isHexColor), "not every step is a colour: $shades")
    }

    @Test
    fun `a shade of something that is not a colour is that same non-colour`() {
        assertEquals("teal", "teal".lightened(0.2))
        assertEquals(listOf("teal", "teal"), "teal".shades(2))
    }

    @Test
    fun `black and white cannot be pushed past the ends of the range`() {
        assertEquals("#ffffff", "#ffffff".lightened(0.5))
        assertEquals("#000000", "#000000".lightened(-0.5))
    }
}
