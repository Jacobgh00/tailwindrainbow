package dev.tailwindrainbow.intellij.domain.theme

import kotlin.math.abs

internal data class Rgb(val red: Double, val green: Double, val blue: Double)

internal data class Hsl(val hue: Double, val saturation: Double, val lightness: Double)

internal fun String.toRgbOrNull(): Rgb? {
    if (!isHexColor()) return null

    val (red, green, blue) = drop(1).chunked(HEX_DIGITS_PER_CHANNEL).map { it.toInt(HEX) / MAX_CHANNEL }

    return Rgb(red, green, blue)
}

internal fun Rgb.toHex(): String = "#%02x%02x%02x".format(red.toChannel(), green.toChannel(), blue.toChannel())

internal fun Rgb.toHsl(): Hsl {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val span = max - min
    val lightness = (max + min) / 2

    if (span == 0.0) {
        return Hsl(hue = 0.0, saturation = 0.0, lightness = lightness)
    }

    val hue =
        when (max) {
            red -> (green - blue) / span
            green -> GREEN_SECTOR + (blue - red) / span
            else -> BLUE_SECTOR + (red - green) / span
        }

    return Hsl(
        hue = (hue * DEGREES_PER_SECTOR + FULL_CIRCLE) % FULL_CIRCLE,
        saturation = span / (1 - abs(2 * lightness - 1)),
        lightness = lightness,
    )
}

internal fun Hsl.toRgb(): Rgb {
    val chroma = (1 - abs(2 * lightness - 1)) * saturation
    val sector = hue / DEGREES_PER_SECTOR
    val second = chroma * (1 - abs(sector % 2 - 1))
    val offset = lightness - chroma / 2

    val sectors =
        listOf(
            Triple(chroma, second, 0.0),
            Triple(second, chroma, 0.0),
            Triple(0.0, chroma, second),
            Triple(0.0, second, chroma),
            Triple(second, 0.0, chroma),
            Triple(chroma, 0.0, second),
        )
    val (red, green, blue) = sectors[sector.toInt().coerceIn(0, sectors.lastIndex)]

    return Rgb(red + offset, green + offset, blue + offset)
}

private const val HEX = 16
private const val HEX_DIGITS_PER_CHANNEL = 2
private const val MAX_CHANNEL = 255.0
private const val DEGREES_PER_SECTOR = 60.0
private const val FULL_CIRCLE = 360.0
private const val GREEN_SECTOR = 2.0
private const val BLUE_SECTOR = 4.0

private fun Double.toChannel(): Int = (this * MAX_CHANNEL).toInt().coerceIn(0, MAX_CHANNEL.toInt())
