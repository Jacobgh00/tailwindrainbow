package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemePreviewTest {
    private val everySection =
        RainbowTheme(
            prefix = mapOf("hover" to style("#111111"), "sm" to style("#222222"), "lg" to style("#333333")),
            base = mapOf("bg-*" to style("#444444")),
            arbitrary = style("#555555"),
            important = style("#666666"),
        )

    @Test
    fun `the sample shows every section a theme can colour`() {
        val kinds = previewSegments(everySection).map { it.kind }.toSet()

        assertEquals(SegmentKind.entries.toSet(), kinds, "a section with no sample text cannot be previewed")
    }

    @Test
    fun `a class list the user types is coloured the same way the sample is`() {
        val typed = previewSegments(everySection, "lg:hover:bg-red-500 unknown-variant:p-4")

        assertEquals(listOf("lg", "hover", "bg-*"), typed.map { it.themeKey })
    }

    @Test
    fun `typing nothing previews nothing`() {
        assertTrue(previewSegments(everySection, "").isEmpty())
    }

    @Test
    fun `an empty theme previews nothing rather than failing`() {
        assertTrue(previewSegments(RainbowTheme()).isEmpty())
    }

    @Test
    fun `the preview reads the palette as it is being edited`() {
        val edited =
            ThemeEditorModel(everySection)
                .restyle(SegmentKind.PREFIX, "hover", RowStyle("#abcdef"))

        val hover = previewSegments(edited.palette()).first { it.themeKey == "hover" }

        assertEquals("#abcdef", hover.style.color)
    }

    @Test
    fun `a row switched off is left out of the palette entirely`() {
        val edited =
            ThemeEditorModel(everySection)
                .restyle(SegmentKind.PREFIX, "hover", RowStyle("#111111", enabled = false))

        assertTrue(previewSegments(edited.palette()).none { it.themeKey == "hover" })
    }

    @Test
    fun `a malformed stored colour is skipped instead of breaking the preview`() {
        val broken = ThemeEditorModel(everySection).restyle(SegmentKind.PREFIX, "sm", RowStyle("not-a-colour"))

        val previewed = previewSegments(broken.palette())

        assertTrue(previewed.none { it.themeKey == "sm" })
        assertTrue(previewed.any { it.themeKey == "hover" }, "one bad entry must not cost the rest")
    }

    private fun style(color: String) = TextStyle(color, FontWeight.BOLD)
}
