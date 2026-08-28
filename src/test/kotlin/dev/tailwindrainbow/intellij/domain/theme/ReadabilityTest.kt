package dev.tailwindrainbow.intellij.domain.theme

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ReadabilityTest {
    private val darkBackground = "#1e1f22"
    private val lightBackground = "#ffffff"

    @Test
    fun `a colour that already reads against the background is left exactly as chosen`() {
        val style = TextStyle("#4ee585", FontWeight.BOLD)

        assertEquals(style, style.readableOn(darkBackground))
    }

    @Test
    fun `a colour too pale for a light background is darkened until it reads`() {
        val style = TextStyle("#4ee585", FontWeight.BOLD)

        val adjusted = style.readableOn(lightBackground)

        assertTrue(adjusted.color != style.color, "an unreadable colour must move")
        assertTrue(
            contrastOf(adjusted.color, lightBackground) >= MINIMUM_CONTRAST,
            "still unreadable at ${adjusted.color}: ${contrastOf(adjusted.color, lightBackground)}",
        )
    }

    @Test
    fun `a colour too dark for a dark background is lightened until it reads`() {
        val style = TextStyle("#101418", FontWeight.BOLD)

        val adjusted = style.readableOn(darkBackground)

        assertTrue(
            contrastOf(adjusted.color, darkBackground) >= MINIMUM_CONTRAST,
            "still unreadable at ${adjusted.color}",
        )
    }

    @Test
    fun `adjusting keeps the colour the user chose recognisable`() {
        val green = TextStyle("#4ee585", FontWeight.BOLD)

        val adjusted = green.readableOn(lightBackground)

        assertTrue(
            abs(hueOf(adjusted.color) - hueOf(green.color)) < HUE_TOLERANCE,
            "hue moved from ${hueOf(green.color)} to ${hueOf(adjusted.color)}",
        )
    }

    @Test
    fun `everything but the colour survives`() {
        val style = TextStyle("#4ee585", FontWeight.BOLD, enabled = false)

        val adjusted = style.readableOn(lightBackground)

        assertEquals(FontWeight.BOLD, adjusted.fontWeight)
        assertEquals(false, adjusted.enabled)
    }

    @Test
    fun `a background nobody can read is no reason to change anything`() {
        val style = TextStyle("#4ee585", FontWeight.BOLD)

        assertEquals(style, style.readableOn("not-a-colour"))
    }

    private fun contrastOf(
        first: String,
        second: String,
    ): Double {
        val lighter = maxOf(luminanceOf(first), luminanceOf(second))
        val darker = minOf(luminanceOf(first), luminanceOf(second))

        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun luminanceOf(hex: String): Double =
        listOf(1..2, 3..4, 5..6)
            .map { hex.substring(it.first, it.last + 1).toInt(16) / 255.0 }
            .map { channel -> if (channel <= 0.03928) channel / 12.92 else Math.pow((channel + 0.055) / 1.055, 2.4) }
            .let { (red, green, blue) -> 0.2126 * red + 0.7152 * green + 0.0722 * blue }

    private fun hueOf(hex: String): Double {
        val (red, green, blue) = listOf(1..2, 3..4, 5..6).map { hex.substring(it.first, it.last + 1).toInt(16) / 255.0 }
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val span = max - min

        if (span == 0.0) return 0.0

        val hue =
            when (max) {
                red -> (green - blue) / span
                green -> 2 + (blue - red) / span
                else -> 4 + (red - green) / span
            }

        return (hue * 60 + 360) % 360
    }

    private companion object {
        const val MINIMUM_CONTRAST = 4.5
        const val HUE_TOLERANCE = 5.0
    }
}
