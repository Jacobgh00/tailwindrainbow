package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

object BuiltInThemes : ThemeSource {
    const val DEFAULT_NAME = "default"
    const val SYNTHWAVE_NAME = "synthwave"
    const val COLOUR_BLIND_NAME = "colour-blind"

    val default =
        Palette(
            arbitrary = "#ff9987",
            important = "#ff0000",
            universal = "#ff0000",
            breakpoints = listOf("#d18bfa", "#b88bfa", "#a78bfa", "#8b8bfa", "#8b9dfa"),
            container = "#6366f1",
            before = "#ffa357",
            after = "#f472b6",
            interaction = listOf("#4ee585", "#4ee6b8", "#49d5e0"),
            visited = "#35c3d6",
            formStates = listOf("#ffe279", "#e3f582", "#c8f66c", "#ff8d8d", "#ff7777", "#ff6969"),
            structural = listOf("#7dd3fc", "#4cc7fc", "#38bdf8", "#24b0f0", "#0ea5e9", "#0284c7"),
            attributes = listOf("#e879f9", "#d946ef", "#c026d3"),
            open = "#a3e635",
            inert = "#9ca3af",
            starting = "#fbbf24",
            media = "#a5b6cd",
            direction = "#94a3b8",
        ).toTheme()

    val synthwave =
        Palette(
            arbitrary = "#ff3308",
            important = "#ff0000",
            importantWeight = FontWeight.BLACK,
            universal = "#ff0000",
            breakpoints = listOf("#ff71ce", "#ff2fb9", "#ff00a4", "#df008f", "#bf007a"),
            container = "#7b2fff",
            before = "#ff9e4f",
            after = "#ff6b21",
            interaction = listOf("#b967ff", "#a742ff", "#951dff"),
            visited = "#7b5cff",
            formStates = listOf("#ff2182", "#ff1e69", "#ff1a50", "#ff1737", "#ff141e", "#ff1105"),
            structural = listOf("#00ffff", "#00e5ff", "#00ccff", "#00b2ff", "#0099ff", "#0066ff"),
            attributes = listOf("#ff5cf4", "#ff2ee8", "#e000d6"),
            open = "#b6ff3d",
            inert = "#7a7f9e",
            starting = "#ffb400",
            media = "#5d6ca7",
            direction = "#8a93c7",
        ).toTheme()

    /** Okabe–Ito hues, one per family, so the families stay apart under all three dichromacies. */
    val colourBlind =
        Palette(
            arbitrary = "#cc79a7",
            important = "#d55e00",
            universal = "#d55e00",
            breakpoints = listOf("#89c0f0", "#67ace0", "#4599d1", "#2385c2", "#0072b2"),
            container = "#005a8f",
            before = "#e69f00",
            after = "#b87d00",
            interaction = listOf("#009e73", "#00b584", "#00cc95"),
            visited = "#6fbfa5",
            formStates = listOf("#f0906b", "#e2764b", "#d55e00", "#b85000", "#9c4400", "#803800"),
            structural = listOf("#a8d8f5", "#86c7ef", "#56b4e9", "#3f9fd6", "#2a8ac2", "#1675ad"),
            attributes = listOf("#cc79a7", "#b35f8d", "#994a75"),
            open = "#009e73",
            inert = "#9e9e9e",
            starting = "#f0e442",
            media = "#8a8a8a",
            direction = "#b5b5b5",
        ).toTheme()

    private val byName =
        linkedMapOf(
            DEFAULT_NAME to default,
            SYNTHWAVE_NAME to synthwave,
            COLOUR_BLIND_NAME to colourBlind,
        )

    override fun themes(): Map<String, RainbowTheme> = byName
}

internal data class Palette(
    val arbitrary: String,
    val important: String,
    val universal: String,
    val breakpoints: List<String>,
    val container: String,
    val before: String,
    val after: String,
    val interaction: List<String>,
    val visited: String,
    val formStates: List<String>,
    val structural: List<String>,
    val attributes: List<String>,
    val open: String,
    val inert: String,
    val starting: String,
    val media: String,
    val direction: String,
    val importantWeight: FontWeight = FontWeight.BOLD,
) {
    init {
        require(breakpoints.size == BREAKPOINTS) { "a palette needs $BREAKPOINTS breakpoint colours" }
        require(interaction.size == INTERACTIONS) { "a palette needs $INTERACTIONS interaction colours" }
        require(formStates.size == FORM_STATES) { "a palette needs $FORM_STATES form state colours" }
        require(structural.size == STRUCTURAL) { "a palette needs $STRUCTURAL structural colours" }
        require(attributes.size == ATTRIBUTES) { "a palette needs $ATTRIBUTES attribute colours" }
    }

    fun toTheme(): RainbowTheme =
        RainbowTheme(
            arbitrary = style(arbitrary),
            important = style(important, importantWeight),
            prefix = universalSelectors() + responsive() + pseudo() + states() + attributesAndModes(),
        )

    private fun universalSelectors() = family(universal, "*", "**")

    private fun responsive(): Map<String, TextStyle> {
        val sizes = listOf("sm", "md", "lg", "xl", "2xl")
        val widths = sizes.zip(breakpoints).toMap()

        return widths.mapValues { (_, colour) -> style(colour) } +
            widths.mapKeys { (size, _) -> "max-$size" }.mapValues { (_, colour) -> style(colour) } +
            family(breakpoints.first(), "min-*", "max-*") +
            family(container, "@*")
    }

    private fun pseudo() =
        family(before, "before", "first-letter", "first-line", "marker", "selection", "file", "backdrop") +
            family(after, "after")

    private fun states(): Map<String, TextStyle> =
        family(interaction[0], "hover") +
            family(interaction[1], "focus", "focus-*") +
            family(interaction[2], "active") +
            family(visited, "visited", "target") +
            family(formStates[PLACEHOLDER], "placeholder") +
            family(formStates[CHECKED], "checked") +
            family(formStates[INVALID], "invalid") +
            family(formStates[DISABLED], "disabled") +
            family(formStates[REQUIRED], "required") +
            family(
                formStates[VALID],
                "valid", "enabled", "indeterminate", "default", "optional", "read-only",
                "autofill", "placeholder-shown", "details-content", "user-*", "*-range", "in-range",
            ) +
            structuralPositions()

    private fun structuralPositions(): Map<String, TextStyle> {
        val positions = listOf("first", "last", "only", "odd", "even", "nth-*")

        return positions.zip(structural).associate { (position, colour) -> position to style(colour) } +
            family(structural[ONLY], "*-of-type", "empty")
    }

    private fun attributesAndModes() =
        family(attributes[0], "data-*") +
            family(attributes[1], "aria-*") +
            family(attributes[2], "supports-*") +
            family(open, "open") +
            family(inert, "inert") +
            family(starting, "starting") +
            family(
                media,
                "dark", "motion-*", "contrast-*", "*-colors", "pointer-*", "any-pointer-*",
                "portrait", "landscape", "noscript", "print",
            ) +
            family(direction, "rtl", "ltr")

    private companion object {
        const val PLACEHOLDER = 0
        const val CHECKED = 1
        const val VALID = 2
        const val INVALID = 3
        const val DISABLED = 4
        const val REQUIRED = 5
        const val ONLY = 2

        const val BREAKPOINTS = 5
        const val INTERACTIONS = 3
        const val FORM_STATES = 6
        const val STRUCTURAL = 6
        const val ATTRIBUTES = 3
    }
}

private fun family(
    color: String,
    vararg keys: String,
) = keys.associateWith { style(color) }

private fun style(
    color: String,
    weight: FontWeight = FontWeight.BOLD,
) = TextStyle(color, weight)
