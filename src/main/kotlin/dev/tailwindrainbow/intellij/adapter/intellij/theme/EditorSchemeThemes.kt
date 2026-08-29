package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.TextAttributesKey
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.toHex
import dev.tailwindrainbow.intellij.adapter.theme.Palette
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
                breakpoints = of(Syntax.NUMBER).shades(BREAKPOINTS),
                container = of(Syntax.NUMBER).lightened(-DEEPER),
                before = of(Syntax.INSTANCE_FIELD, Syntax.IDENTIFIER),
                after = of(Syntax.INSTANCE_FIELD, Syntax.IDENTIFIER).lightened(-DEEPER),
                interaction = of(Syntax.KEYWORD).shades(INTERACTIONS),
                visited = of(Syntax.KEYWORD).lightened(LIGHTER),
                formStates = of(Syntax.STRING).shades(FORM_STATES),
                structural = of(Syntax.FUNCTION_DECLARATION).shades(STRUCTURAL),
                attributes = of(Syntax.METADATA).shades(ATTRIBUTES),
                open = of(Syntax.CONSTANT),
                inert = of(Syntax.LINE_COMMENT),
                starting = of(Syntax.LABEL, Syntax.CONSTANT),
                media = of(Syntax.DOC_COMMENT, Syntax.LINE_COMMENT),
                direction = of(Syntax.LINE_COMMENT).lightened(LIGHTER),
            ).toTheme()
        }

    private const val BREAKPOINTS = 5
    private const val INTERACTIONS = 3
    private const val FORM_STATES = 6
    private const val STRUCTURAL = 6
    private const val ATTRIBUTES = 3
    private const val LIGHTER = 0.12
    private const val DEEPER = 0.12
}

private class SchemeColours(private val scheme: EditorColorsScheme) {
    fun of(vararg keys: TextAttributesKey): String =
        keys.firstNotNullOfOrNull { scheme.getAttributes(it)?.foregroundColor }?.toHex()
            ?: scheme.defaultForeground.toHex()
}
