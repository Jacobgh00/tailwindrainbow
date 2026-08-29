package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.openapi.editor.markup.TextAttributes
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.readableOn
import java.awt.Color
import java.awt.Font

private const val BOLD_WEIGHT_THRESHOLD = 600

internal fun TextStyle.toTextAttributes(background: Color): TextAttributes =
    TextAttributes().apply {
        foregroundColor = Color.decode(readableOn(background.toHex()).color)
        fontType = if (fontWeight.value >= BOLD_WEIGHT_THRESHOLD) Font.BOLD else Font.PLAIN
    }

internal fun Color.toHex(): String = "#%02x%02x%02x".format(red, green, blue)
