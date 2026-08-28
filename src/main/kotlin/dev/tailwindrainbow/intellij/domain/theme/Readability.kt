package dev.tailwindrainbow.intellij.domain.theme

import kotlin.math.abs
import kotlin.math.pow

/**
 * This style, moved far enough from [background] to be readable against it.
 *
 * A palette is chosen once and read on whatever background the IDE happens to be using, and the
 * colours that make variants stand out on a dark editor are the same colours that disappear into a
 * light one. Rather than ask for a second palette nobody would keep up to date — and which a theme
 * contributed by another plugin could not supply at all — the colour is moved only along its
 * lightness, only when it falls below [MINIMUM_CONTRAST], and only far enough to clear it. The hue
 * the user picked survives; its legibility stops depending on which scheme they are in.
 *
 * A colour or background that is not `#RRGGBB` is left alone. Reporting a malformed colour belongs
 * to whatever parsed it; making it readable is impossible and pretending otherwise would hide it.
 */
fun TextStyle.readableOn(background: String): TextStyle {
    val backgroundColor = background.toRgb() ?: return this
    val chosen = color.toRgb() ?: return this

    if (contrast(chosen, backgroundColor) >= MINIMUM_CONTRAST) {
        return this
    }

    return copy(color = chosen.movedAwayFrom(backgroundColor).toHex())
}

/** WCAG AA for text. Below this a variant is present but unreadable, which is worse than uncoloured. */
private const val MINIMUM_CONTRAST = 4.5

private const val LIGHTNESS_STEP = 0.02
private const val LIGHT_BACKGROUND = 0.5

/** Steps to walk the whole lightness range, so the loop cannot end before the range does. */
private const val MAX_STEPS = (1.0 / LIGHTNESS_STEP).toInt() + 1

private fun Rgb.movedAwayFrom(background: Rgb): Rgb {
    val step = if (background.luminance() > LIGHT_BACKGROUND) -LIGHTNESS_STEP else LIGHTNESS_STEP
    var moved = toHsl()

    repeat(MAX_STEPS) {
        moved = moved.copy(lightness = (moved.lightness + step).coerceIn(0.0, 1.0))
        val candidate = moved.toRgb()

        if (contrast(candidate, background) >= MINIMUM_CONTRAST) return candidate
    }

    return moved.toRgb()
}

private fun contrast(
    first: Rgb,
    second: Rgb,
): Double {
    val lighter = maxOf(first.luminance(), second.luminance())
    val darker = minOf(first.luminance(), second.luminance())

    return (lighter + CONTRAST_FLOOR) / (darker + CONTRAST_FLOOR)
}

private const val HEX = 16
private const val HEX_DIGITS_PER_CHANNEL = 2
private const val MAX_CHANNEL = 255.0

/** The constants of the WCAG contrast and luminance formulas, which are the formulas, not choices. */
private const val CONTRAST_FLOOR = 0.05
private const val SRGB_KNEE = 0.03928
private const val SRGB_SLOPE = 12.92
private const val SRGB_OFFSET = 0.055
private const val SRGB_SCALE = 1.055
private const val SRGB_GAMMA = 2.4
private const val RED_LUMINANCE = 0.2126
private const val GREEN_LUMINANCE = 0.7152
private const val BLUE_LUMINANCE = 0.0722

private const val DEGREES_PER_SECTOR = 60.0
private const val FULL_CIRCLE = 360.0

/** Where each channel's sector starts on the wheel, counted in sectors from red. */
private const val GREEN_SECTOR = 2.0
private const val BLUE_SECTOR = 4.0

private data class Rgb(val red: Double, val green: Double, val blue: Double)

private data class Hsl(val hue: Double, val saturation: Double, val lightness: Double)

private fun String.toRgb(): Rgb? {
    if (!isHexColor()) return null

    val (red, green, blue) = drop(1).chunked(HEX_DIGITS_PER_CHANNEL).map { it.toInt(HEX) / MAX_CHANNEL }

    return Rgb(red, green, blue)
}

private fun Rgb.toHex(): String = "#%02x%02x%02x".format(red.toChannel(), green.toChannel(), blue.toChannel())

private fun Double.toChannel(): Int = (this * MAX_CHANNEL).toInt().coerceIn(0, MAX_CHANNEL.toInt())

/** WCAG relative luminance: how bright a colour reads, rather than how large its numbers are. */
private fun Rgb.luminance(): Double {
    return RED_LUMINANCE * red.weighed() + GREEN_LUMINANCE * green.weighed() + BLUE_LUMINANCE * blue.weighed()
}

private fun Double.weighed(): Double {
    return if (this <= SRGB_KNEE) this / SRGB_SLOPE else ((this + SRGB_OFFSET) / SRGB_SCALE).pow(SRGB_GAMMA)
}

private fun Rgb.toHsl(): Hsl {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val span = max - min
    val lightness = (max + min) / 2

    if (span == 0.0) return Hsl(hue = 0.0, saturation = 0.0, lightness = lightness)

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

private fun Hsl.toRgb(): Rgb {
    val chroma = (1 - abs(2 * lightness - 1)) * saturation
    val sector = hue / DEGREES_PER_SECTOR
    val second = chroma * (1 - abs(sector % 2 - 1))
    val offset = lightness - chroma / 2

    // The six sectors of the colour wheel, each naming which channel rises, which falls, and which rests.
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
