package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

object BuiltInThemes : ThemeSource {
    const val DEFAULT_NAME = "default"
    const val SYNTHWAVE_NAME = "synthwave"

    val default =
        RainbowTheme(
            arbitrary = style("#ff9987"),
            important = style("#ff0000"),
            prefix =
                mapOf(
                    "*" to style("#ff0000"),
                    "**" to style("#ff0000"),
                    "min-*" to style("#d18bfa"),
                    "sm" to style("#d18bfa"),
                    "md" to style("#b88bfa"),
                    "lg" to style("#a78bfa"),
                    "xl" to style("#8b8bfa"),
                    "2xl" to style("#8b9dfa"),
                    "max-*" to style("#d18bfa"),
                    "max-sm" to style("#d18bfa"),
                    "max-md" to style("#b88bfa"),
                    "max-lg" to style("#a78bfa"),
                    "max-xl" to style("#8b8bfa"),
                    "max-2xl" to style("#8b9dfa"),
                    "before" to style("#ffa357"),
                    "after" to style("#f472b6"),
                    "hover" to style("#4ee585"),
                    "focus" to style("#4ee6b8"),
                    "active" to style("#49d5e0"),
                    "dark" to style("#a5b6cd"),
                    "placeholder" to style("#ffe279"),
                    "checked" to style("#e3f582"),
                    "valid" to style("#c8f66c"),
                    "invalid" to style("#ff8d8d"),
                    "disabled" to style("#ff7777"),
                    "required" to style("#ff6969"),
                    "first" to style("#7dd3fc"),
                    "last" to style("#4cc7fc"),
                    "only" to style("#38bdf8"),
                    "odd" to style("#24b0f0"),
                    "even" to style("#0ea5e9"),
                    "nth-*" to style("#0284c7"),
                    "@*" to style("#6366f1"),
                    "data-*" to style("#e879f9"),
                    "aria-*" to style("#d946ef"),
                    "supports-*" to style("#c026d3"),
                    "open" to style("#a3e635"),
                    "inert" to style("#9ca3af"),
                    "starting" to style("#fbbf24"),
                ) +
                    family("#4ee6b8", "focus-*") +
                    family("#35c3d6", "visited", "target") +
                    family("#38bdf8", "*-of-type", "empty") +
                    family(
                        "#c8f66c",
                        "enabled", "indeterminate", "default", "optional", "read-only",
                        "autofill", "placeholder-shown", "details-content", "user-*", "*-range", "in-range",
                    ) +
                    family(
                        "#ffa357",
                        "first-letter", "first-line", "marker", "selection", "file", "backdrop",
                    ) +
                    family(
                        "#a5b6cd",
                        "motion-*", "contrast-*", "*-colors", "pointer-*", "any-pointer-*",
                        "portrait", "landscape", "noscript", "print",
                    ) +
                    family("#94a3b8", "rtl", "ltr"),
        )

    val synthwave =
        RainbowTheme(
            arbitrary = style("#ff3308"),
            important = style("#ff0000", FontWeight.BLACK),
            prefix =
                mapOf(
                    "*" to style("#ff0000"),
                    "**" to style("#ff0000"),
                    "min-*" to style("#ff71ce"),
                    "sm" to style("#ff71ce"),
                    "md" to style("#ff2fb9"),
                    "lg" to style("#ff00a4"),
                    "xl" to style("#df008f"),
                    "2xl" to style("#bf007a"),
                    "max-*" to style("#ff71ce"),
                    "max-sm" to style("#ff71ce"),
                    "max-md" to style("#ff2fb9"),
                    "max-lg" to style("#ff00a4"),
                    "max-xl" to style("#df008f"),
                    "max-2xl" to style("#bf007a"),
                    "before" to style("#ff9e4f"),
                    "after" to style("#ff6b21"),
                    "hover" to style("#b967ff"),
                    "focus" to style("#a742ff"),
                    "active" to style("#951dff"),
                    "dark" to style("#5d6ca7"),
                    "placeholder" to style("#ff2182"),
                    "checked" to style("#ff1e69"),
                    "valid" to style("#ff1a50"),
                    "invalid" to style("#ff1737"),
                    "disabled" to style("#ff141e"),
                    "required" to style("#ff1105"),
                    "first" to style("#00ffff"),
                    "last" to style("#00e5ff"),
                    "only" to style("#00ccff"),
                    "odd" to style("#00b2ff"),
                    "even" to style("#0099ff"),
                    "nth-*" to style("#0066ff"),
                    "@*" to style("#7b2fff"),
                    "data-*" to style("#ff5cf4"),
                    "aria-*" to style("#ff2ee8"),
                    "supports-*" to style("#e000d6"),
                    "open" to style("#b6ff3d"),
                    "inert" to style("#7a7f9e"),
                    "starting" to style("#ffb400"),
                ) +
                    family("#a742ff", "focus-*") +
                    family("#7b5cff", "visited", "target") +
                    family("#00ccff", "*-of-type", "empty") +
                    family(
                        "#ff1a50",
                        "enabled", "indeterminate", "default", "optional", "read-only",
                        "autofill", "placeholder-shown", "details-content", "user-*", "*-range", "in-range",
                    ) +
                    family(
                        "#ff9e4f",
                        "first-letter", "first-line", "marker", "selection", "file", "backdrop",
                    ) +
                    family(
                        "#5d6ca7",
                        "motion-*", "contrast-*", "*-colors", "pointer-*", "any-pointer-*",
                        "portrait", "landscape", "noscript", "print",
                    ) +
                    family("#8a93c7", "rtl", "ltr"),
        )

    private val byName =
        linkedMapOf(
            DEFAULT_NAME to default,
            SYNTHWAVE_NAME to synthwave,
        )

    override fun themes(): Map<String, RainbowTheme> = byName
}

private fun family(
    color: String,
    vararg keys: String,
) = keys.associateWith { style(color) }

private fun style(
    color: String,
    weight: FontWeight = FontWeight.BOLD,
) = TextStyle(color, weight)
