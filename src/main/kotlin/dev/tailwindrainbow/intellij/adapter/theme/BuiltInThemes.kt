package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

object BuiltInThemes : ThemeSource {
    const val DEFAULT_NAME = "default"
    const val SYNTHWAVE_NAME = "synthwave"
    const val COLOUR_BLIND_NAME = "colour-blind"

    val default =
        Palette(
            arbitrary = "#ff9987",
            important = "#ff0000",
            universal = "#ff0000",
            breakpoints =
                BreakpointColors(
                    sm = "#d18bfa",
                    md = "#b88bfa",
                    lg = "#a78bfa",
                    xl = "#8b8bfa",
                    twoXl = "#8b9dfa",
                ),
            container = "#6366f1",
            before = "#ffa357",
            after = "#f472b6",
            interaction = InteractionColors(hover = "#4ee585", focus = "#4ee6b8", active = "#49d5e0"),
            visited = "#35c3d6",
            formStates =
                FormStateColors(
                    placeholder = "#ffe279",
                    checked = "#e3f582",
                    valid = "#c8f66c",
                    invalid = "#ff8d8d",
                    disabled = "#ff7777",
                    required = "#ff6969",
                ),
            structural =
                StructuralColors(
                    first = "#7dd3fc",
                    last = "#4cc7fc",
                    only = "#38bdf8",
                    odd = "#24b0f0",
                    even = "#0ea5e9",
                    nth = "#0284c7",
                ),
            attributes = AttributeColors(data = "#e879f9", aria = "#d946ef", supports = "#c026d3"),
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
            breakpoints =
                BreakpointColors(
                    sm = "#ff71ce",
                    md = "#ff2fb9",
                    lg = "#ff00a4",
                    xl = "#df008f",
                    twoXl = "#bf007a",
                ),
            container = "#7b2fff",
            before = "#ff9e4f",
            after = "#ff6b21",
            interaction = InteractionColors(hover = "#b967ff", focus = "#a742ff", active = "#951dff"),
            visited = "#7b5cff",
            formStates =
                FormStateColors(
                    placeholder = "#ff2182",
                    checked = "#ff1e69",
                    valid = "#ff1a50",
                    invalid = "#ff1737",
                    disabled = "#ff141e",
                    required = "#ff1105",
                ),
            structural =
                StructuralColors(
                    first = "#00ffff",
                    last = "#00e5ff",
                    only = "#00ccff",
                    odd = "#00b2ff",
                    even = "#0099ff",
                    nth = "#0066ff",
                ),
            attributes = AttributeColors(data = "#ff5cf4", aria = "#ff2ee8", supports = "#e000d6"),
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
            breakpoints =
                BreakpointColors(
                    sm = "#89c0f0",
                    md = "#67ace0",
                    lg = "#4599d1",
                    xl = "#2385c2",
                    twoXl = "#0072b2",
                ),
            container = "#005a8f",
            before = "#e69f00",
            after = "#b87d00",
            interaction = InteractionColors(hover = "#009e73", focus = "#00b584", active = "#00cc95"),
            visited = "#6fbfa5",
            formStates =
                FormStateColors(
                    placeholder = "#f0906b",
                    checked = "#e2764b",
                    valid = "#d55e00",
                    invalid = "#b85000",
                    disabled = "#9c4400",
                    required = "#803800",
                ),
            structural =
                StructuralColors(
                    first = "#a8d8f5",
                    last = "#86c7ef",
                    only = "#56b4e9",
                    odd = "#3f9fd6",
                    even = "#2a8ac2",
                    nth = "#1675ad",
                ),
            attributes = AttributeColors(data = "#cc79a7", aria = "#b35f8d", supports = "#994a75"),
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
        ).toMap()

    override fun themes(): Map<String, RainbowTheme> = byName
}
