package dev.tailwindrainbow.intellij.domain

object RainbowThemes {
    const val DEFAULT_NAME = "default"
    const val SYNTHWAVE_NAME = "synthwave"

    val names: Set<String> = linkedSetOf(DEFAULT_NAME, SYNTHWAVE_NAME)

    fun find(name: String): RainbowTheme = when (name) {
        SYNTHWAVE_NAME -> synthwave
        else -> default
    }

    val default = RainbowTheme(
        arbitrary = style("#ff9987"),
        important = style("#ff0000"),
        prefix = mapOf(
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
        ),
    )

    val synthwave = RainbowTheme(
        arbitrary = style("#ff3308"),
        important = style("#ff0000", FontWeight.of(900)),
        prefix = mapOf(
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
        ),
    )
}

private fun style(color: String, weight: FontWeight = FontWeight.BOLD) = TextStyle(color, weight)
