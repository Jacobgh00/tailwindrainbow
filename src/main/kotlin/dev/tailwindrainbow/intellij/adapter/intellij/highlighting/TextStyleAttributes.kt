package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.openapi.editor.markup.TextAttributes
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.readableOn
import java.awt.Color
import java.awt.Font

/** Swing renders only regular and bold, so every weight at or above this maps to bold. */
private const val BOLD_WEIGHT_THRESHOLD = 600

/**
 * The only place the domain palette meets AWT, which is why the domain stores colors as
 * `#RRGGBB` strings and never imports anything from the platform.
 *
 * [background] is the editor's own, so a palette chosen for one scheme stays readable in another.
 */
internal fun TextStyle.toTextAttributes(background: Color): TextAttributes =
    TextAttributes().apply {
        foregroundColor = Color.decode(readableOn(background.toHex()).color)
        fontType = if (fontWeight.value >= BOLD_WEIGHT_THRESHOLD) Font.BOLD else Font.PLAIN
    }

private fun Color.toHex(): String = "#%02x%02x%02x".format(red, green, blue)
