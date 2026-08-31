package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import dev.tailwindrainbow.intellij.adapter.color.toHex
import dev.tailwindrainbow.intellij.adapter.theme.AttributeColors
import dev.tailwindrainbow.intellij.adapter.theme.BreakpointColors
import dev.tailwindrainbow.intellij.adapter.theme.FormStateColors
import dev.tailwindrainbow.intellij.adapter.theme.InteractionColors
import dev.tailwindrainbow.intellij.adapter.theme.Palette
import dev.tailwindrainbow.intellij.adapter.theme.StructuralColors
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.lightened
import dev.tailwindrainbow.intellij.domain.theme.shades
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors as Syntax

object EditorSchemeThemes : ThemeSource {
    const val NAME = "editor scheme"

    override fun themes(): Map<String, RainbowTheme> = mapOf(NAME to themeOf(activeScheme()))

    private fun activeScheme() = EditorColorsManager.getInstance().globalScheme

    internal fun themeOf(scheme: EditorColorsScheme): RainbowTheme =
        with(SchemeColours(scheme)) {
            Palette(
                arbitrary = of(Syntax.MARKUP_TAG, Syntax.CLASS_NAME),
                important = of(Syntax.INVALID_STRING_ESCAPE, Syntax.KEYWORD),
                universal = of(Syntax.INVALID_STRING_ESCAPE, Syntax.KEYWORD),
                breakpoints = BreakpointColors.from(of(Syntax.NUMBER).shades(BreakpointColors.RAMP_SIZE)),
                container = of(Syntax.NUMBER).lightened(-DEEPER),
                before = of(Syntax.INSTANCE_FIELD, Syntax.IDENTIFIER),
                after = of(Syntax.INSTANCE_FIELD, Syntax.IDENTIFIER).lightened(-DEEPER),
                interaction = InteractionColors.from(of(Syntax.KEYWORD).shades(InteractionColors.RAMP_SIZE)),
                visited = of(Syntax.KEYWORD).lightened(LIGHTER),
                formStates = FormStateColors.from(of(Syntax.STRING).shades(FormStateColors.RAMP_SIZE)),
                structural =
                    StructuralColors.from(of(Syntax.FUNCTION_DECLARATION).shades(StructuralColors.RAMP_SIZE)),
                attributes = AttributeColors.from(of(Syntax.METADATA).shades(AttributeColors.RAMP_SIZE)),
                modifier = of(Syntax.OPERATION_SIGN, Syntax.IDENTIFIER),
                open = of(Syntax.CONSTANT),
                inert = of(Syntax.LINE_COMMENT),
                starting = of(Syntax.LABEL, Syntax.CONSTANT),
                media = of(Syntax.DOC_COMMENT, Syntax.LINE_COMMENT),
                direction = of(Syntax.LINE_COMMENT).lightened(LIGHTER),
            ).toTheme()
        }

    private const val LIGHTER = 0.12
    private const val DEEPER = 0.12
}

private class SchemeColours(private val scheme: EditorColorsScheme) {
    fun of(vararg keys: TextAttributesKey): String =
        keys.firstNotNullOfOrNull { scheme.getAttributes(it)?.foregroundColor }?.toHex()
            ?: scheme.defaultForeground.toHex()
}
