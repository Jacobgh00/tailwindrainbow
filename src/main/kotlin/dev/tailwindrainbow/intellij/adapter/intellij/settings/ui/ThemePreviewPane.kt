package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.util.ui.JBUI
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.toTextAttributes
import dev.tailwindrainbow.intellij.application.settings.PREVIEW_SAMPLE
import dev.tailwindrainbow.intellij.application.settings.previewSegments
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import java.awt.Color
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JTextPane
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AttributeSet
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

internal class ThemePreviewPane {
    private val sample =
        JTextPane().apply {
            border = JBUI.Borders.empty(PADDING)
            text = PREVIEW_SAMPLE
            accessibleContext.accessibleName = message("editor.preview.name")
        }

    private var theme = RainbowTheme()

    val component: JComponent = sample

    init {
        sample.document.addDocumentListener(
            object : DocumentListener {
                override fun insertUpdate(event: DocumentEvent) = repaintLater()

                override fun removeUpdate(event: DocumentEvent) = repaintLater()

                override fun changedUpdate(event: DocumentEvent) = Unit
            },
        )
    }

    fun show(palette: RainbowTheme) {
        theme = palette
        repaint()
    }

    fun restoreSample() {
        sample.text = PREVIEW_SAMPLE
    }

    private fun repaintLater() = SwingUtilities.invokeLater(::repaint)

    private fun repaint() {
        val scheme = EditorColorsManager.getInstance().globalScheme
        val background = scheme.defaultBackground

        sample.background = background
        sample.font = scheme.getFont(EditorFontType.PLAIN)

        val document = sample.styledDocument
        document.setCharacterAttributes(0, document.length, foreground(scheme.defaultForeground), true)

        previewSegments(theme, sample.text).forEach { segment ->
            document.setCharacterAttributes(
                segment.start,
                segment.end - segment.start,
                segment.style.asAttributes(background),
                false,
            )
        }
    }

    private fun foreground(color: Color) = SimpleAttributeSet().apply { StyleConstants.setForeground(this, color) }

    private fun TextStyle.asAttributes(background: Color): AttributeSet {
        val painted = toTextAttributes(background)

        return SimpleAttributeSet().apply {
            StyleConstants.setForeground(this, painted.foregroundColor)
            StyleConstants.setBold(this, painted.fontType == Font.BOLD)
        }
    }

    private companion object {
        const val PADDING = 6
    }
}
