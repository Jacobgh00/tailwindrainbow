package dev.tailwindrainbow.intellij.domain.theme

fun String.lightened(by: Double): String {
    val hsl = toRgbOrNull()?.toHsl() ?: return this

    return hsl.copy(lightness = (hsl.lightness + by).coerceIn(0.0, 1.0)).toRgb().toHex()
}

fun String.shades(count: Int): List<String> {
    require(count > 0) { "a ramp needs at least one step" }

    val step = if (count == 1) 0.0 else SHADE_SPAN * 2 / (count - 1)

    return List(count) { index -> lightened(SHADE_SPAN - index * step) }
}

private const val SHADE_SPAN = 0.18
