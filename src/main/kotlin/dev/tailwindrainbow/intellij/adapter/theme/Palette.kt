package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SCOPING_MODIFIERS
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

internal data class BreakpointColors(
    val sm: String,
    val md: String,
    val lg: String,
    val xl: String,
    val twoXl: String,
) {
    companion object {
        const val RAMP_SIZE = 5

        fun from(colors: List<String>): BreakpointColors {
            val ramp = colors.validatedRamp(RAMP_SIZE, "breakpoint colours")
            return BreakpointColors(
                sm = ramp[0],
                md = ramp[1],
                lg = ramp[2],
                xl = ramp[3],
                twoXl = ramp[4],
            )
        }
    }
}

internal data class InteractionColors(
    val hover: String,
    val focus: String,
    val active: String,
) {
    companion object {
        const val RAMP_SIZE = 3

        fun from(colors: List<String>): InteractionColors {
            val (hover, focus, active) = colors.validatedRamp(RAMP_SIZE, "interaction colours")
            return InteractionColors(hover, focus, active)
        }
    }
}

internal data class FormStateColors(
    val placeholder: String,
    val checked: String,
    val valid: String,
    val invalid: String,
    val disabled: String,
    val required: String,
) {
    companion object {
        const val RAMP_SIZE = 6

        fun from(colors: List<String>): FormStateColors {
            val ramp = colors.validatedRamp(RAMP_SIZE, "form state colours")
            return FormStateColors(
                placeholder = ramp[0],
                checked = ramp[1],
                valid = ramp[2],
                invalid = ramp[3],
                disabled = ramp[4],
                required = ramp[5],
            )
        }
    }
}

internal data class StructuralColors(
    val first: String,
    val last: String,
    val only: String,
    val odd: String,
    val even: String,
    val nth: String,
) {
    companion object {
        const val RAMP_SIZE = 6

        fun from(colors: List<String>): StructuralColors {
            val ramp = colors.validatedRamp(RAMP_SIZE, "structural colours")
            return StructuralColors(
                first = ramp[0],
                last = ramp[1],
                only = ramp[2],
                odd = ramp[3],
                even = ramp[4],
                nth = ramp[5],
            )
        }
    }
}

internal data class AttributeColors(
    val data: String,
    val aria: String,
    val supports: String,
) {
    companion object {
        const val RAMP_SIZE = 3

        fun from(colors: List<String>): AttributeColors {
            val (data, aria, supports) = colors.validatedRamp(RAMP_SIZE, "attribute colours")
            return AttributeColors(data, aria, supports)
        }
    }
}

internal data class Palette(
    val arbitrary: String,
    val important: String,
    val universal: String,
    val breakpoints: BreakpointColors,
    val container: String,
    val before: String,
    val after: String,
    val interaction: InteractionColors,
    val visited: String,
    val formStates: FormStateColors,
    val structural: StructuralColors,
    val attributes: AttributeColors,
    val modifier: String,
    val open: String,
    val inert: String,
    val starting: String,
    val media: String,
    val direction: String,
    val importantWeight: FontWeight = FontWeight.BOLD,
) {
    fun toTheme(): RainbowTheme =
        RainbowTheme(
            arbitrary = style(arbitrary),
            important = style(important, importantWeight),
            prefix =
                universalSelectors() + responsive() + pseudo() + states() + attributesAndModes() + modifiers(),
        )

    private fun universalSelectors() = family(universal, "*", "**")

    private fun modifiers() = SCOPING_MODIFIERS.associateWith { style(modifier) }

    private fun responsive(): Map<String, TextStyle> {
        val widths =
            mapOf(
                "sm" to breakpoints.sm,
                "md" to breakpoints.md,
                "lg" to breakpoints.lg,
                "xl" to breakpoints.xl,
                "2xl" to breakpoints.twoXl,
            )

        return widths.mapValues { (_, colour) -> style(colour) } +
            widths.mapKeys { (size, _) -> "max-$size" }.mapValues { (_, colour) -> style(colour) } +
            family(breakpoints.sm, "min-*", "max-*") +
            family(container, "@*")
    }

    private fun pseudo() =
        family(before, "before", "first-letter", "first-line", "marker", "selection", "file", "backdrop") +
            family(after, "after")

    private fun states(): Map<String, TextStyle> =
        family(interaction.hover, "hover") +
            family(interaction.focus, "focus", "focus-*") +
            family(interaction.active, "active") +
            family(visited, "visited", "target") +
            family(formStates.placeholder, "placeholder") +
            family(formStates.checked, "checked") +
            family(formStates.invalid, "invalid") +
            family(formStates.disabled, "disabled") +
            family(formStates.required, "required") +
            family(
                formStates.valid,
                "valid", "enabled", "indeterminate", "default", "optional", "read-only",
                "autofill", "placeholder-shown", "details-content", "user-*", "*-range", "in-range",
            ) +
            structuralPositions()

    private fun structuralPositions(): Map<String, TextStyle> {
        val positions =
            mapOf(
                "first" to structural.first,
                "last" to structural.last,
                "only" to structural.only,
                "odd" to structural.odd,
                "even" to structural.even,
                "nth-*" to structural.nth,
            )

        return positions.mapValues { (_, colour) -> style(colour) } +
            family(structural.only, "*-of-type", "empty")
    }

    private fun attributesAndModes() =
        family(attributes.data, "data-*") +
            family(attributes.aria, "aria-*") +
            family(attributes.supports, "supports-*") +
            family(open, "open") +
            family(inert, "inert") +
            family(starting, "starting") +
            family(
                media,
                "dark", "motion-*", "contrast-*", "*-colors", "pointer-*", "any-pointer-*",
                "portrait", "landscape", "noscript", "print",
            ) +
            family(direction, "rtl", "ltr")
}

private fun List<String>.validatedRamp(
    expected: Int,
    label: String,
): List<String> {
    require(size == expected) { "a palette needs $expected $label" }
    return this
}

private fun family(
    color: String,
    vararg keys: String,
) = keys.associateWith { style(color) }

private fun style(
    color: String,
    weight: FontWeight = FontWeight.BOLD,
) = TextStyle(color, weight)
