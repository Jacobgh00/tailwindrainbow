package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.openapi.editor.markup.TextAttributes
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import java.awt.Color
import java.awt.Font

/** Swing renders only regular and bold, so every weight at or above this maps to bold. */
private const val BOLD_WEIGHT_THRESHOLD = 600

/**
 * Adapts a theme's [TextStyle] to the editor's [TextAttributes].
 *
 * The only place the pure domain palette meets AWT, which is why the domain stores colors as
 * `#RRGGBB` strings and never imports anything from the platform.
 */
internal fun TextStyle.toTextAttributes(): TextAttributes = TextAttributes().apply {
    foregroundColor = Color.decode(color)
    fontType = if (fontWeight.value >= BOLD_WEIGHT_THRESHOLD) Font.BOLD else Font.PLAIN
}
