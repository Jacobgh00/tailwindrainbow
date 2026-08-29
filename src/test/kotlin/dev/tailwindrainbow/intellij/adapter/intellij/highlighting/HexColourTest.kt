package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import java.awt.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class HexColourTest {
    @Test
    fun `a colour is written the way a theme writes one`() {
        assertEquals("#4ee585", Color(78, 229, 133).toHex())
    }

    @Test
    fun `each channel keeps both its digits`() {
        assertEquals("#010203", Color(1, 2, 3).toHex())
    }

    @Test
    fun `the ends of the range are written in full`() {
        assertEquals("#000000", Color(0, 0, 0).toHex())
        assertEquals("#ffffff", Color(255, 255, 255).toHex())
    }
}
