package dev.tailwindrainbow.intellij.domain.theme

import kotlin.math.pow

fun TextStyle.readableOn(background: String): TextStyle {
    val backgroundColor = background.toRgbOrNull() ?: return this
    val chosen = color.toRgbOrNull() ?: return this

    if (contrast(chosen, backgroundColor) >= WCAG_AA_CONTRAST) {
        return this
    }

    return copy(color = chosen.movedAwayFrom(backgroundColor).toHex())
}

private const val WCAG_AA_CONTRAST = 4.5

private const val LIGHTNESS_STEP = 0.02
private const val LIGHT_BACKGROUND_LUMINANCE = 0.5

private const val STEPS_ACROSS_LIGHTNESS_RANGE = (1.0 / LIGHTNESS_STEP).toInt() + 1

private fun Rgb.movedAwayFrom(background: Rgb): Rgb {
    val step = if (background.luminance() > LIGHT_BACKGROUND_LUMINANCE) -LIGHTNESS_STEP else LIGHTNESS_STEP
    var moved = toHsl()

    repeat(STEPS_ACROSS_LIGHTNESS_RANGE) {
        moved = moved.copy(lightness = (moved.lightness + step).coerceIn(0.0, 1.0))
        val candidate = moved.toRgb()

        if (contrast(candidate, background) >= WCAG_AA_CONTRAST) return candidate
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

private const val CONTRAST_FLOOR = 0.05
private const val SRGB_KNEE = 0.03928
private const val SRGB_SLOPE = 12.92
private const val SRGB_OFFSET = 0.055
private const val SRGB_SCALE = 1.055
private const val SRGB_GAMMA = 2.4
private const val RED_LUMINANCE = 0.2126
private const val GREEN_LUMINANCE = 0.7152
private const val BLUE_LUMINANCE = 0.0722

private fun Rgb.luminance(): Double {
    return RED_LUMINANCE * red.weighed() + GREEN_LUMINANCE * green.weighed() + BLUE_LUMINANCE * blue.weighed()
}

private fun Double.weighed(): Double {
    return if (this <= SRGB_KNEE) this / SRGB_SLOPE else ((this + SRGB_OFFSET) / SRGB_SCALE).pow(SRGB_GAMMA)
}
