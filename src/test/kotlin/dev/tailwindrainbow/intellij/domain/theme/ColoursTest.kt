package dev.tailwindrainbow.intellij.domain.theme

import kotlin.test.Test
import kotlin.test.assertEquals

class ColoursTest {
    @Test
    fun `RGB channels become a canonical six-digit hex colour`() {
        assertEquals("#010aff", rgbToHex(1, 10, 255))
    }
}
