package dev.tailwindrainbow.intellij.application.theme

internal const val PREFIX = "prefix"
internal const val BASE = "base"
internal const val ARBITRARY = "arbitrary"
internal const val IMPORTANT = "important"
internal const val COLOR = "color"
internal const val FONT_WEIGHT = "fontWeight"
internal const val ENABLED = "enabled"

@Suppress("MagicNumber")
internal val WEIGHT_NAMES =
    mapOf(
        "thin" to 100,
        "extralight" to 200,
        "light" to 300,
        "lighter" to 300,
        "normal" to 400,
        "medium" to 500,
        "semibold" to 600,
        "bold" to 700,
        "bolder" to 700,
        "extrabold" to 800,
        "black" to 900,
    )
