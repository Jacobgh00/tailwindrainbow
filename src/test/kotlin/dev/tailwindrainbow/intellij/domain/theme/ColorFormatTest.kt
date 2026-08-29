package dev.tailwindrainbow.intellij.domain.theme

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ColorFormatTest {
    @Test
    fun `a full hex colour is taken as it is, in one case`() {
        assertEquals("#4ee585", "#4ee585".toHexColorOrNull())
        assertEquals("#4ee585", "#4EE585".toHexColorOrNull())
        assertEquals("#4ee585", "  #4ee585  ".toHexColorOrNull())
    }

    @Test
    fun `the hash is optional, because a pasted colour often arrives without it`() {
        assertEquals("#4ee585", "4ee585".toHexColorOrNull())
    }

    @Test
    fun `shorthand is expanded rather than refused`() {
        assertEquals("#ffffff", "#fff".toHexColorOrNull())
        assertEquals("#aabbcc", "abc".toHexColorOrNull())
    }

    @Test
    fun `what is not a colour stays not a colour`() {
        listOf("red", "", "#12345", "#1234567", "blue-500", "#gggggg").forEach {
            assertNull(it.toHexColorOrNull(), "'$it' is not a hex colour")
        }
    }
}
