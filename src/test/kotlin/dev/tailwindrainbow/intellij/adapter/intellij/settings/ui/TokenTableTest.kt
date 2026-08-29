package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndGet
import dev.tailwindrainbow.intellij.application.settings.RowOrigin
import dev.tailwindrainbow.intellij.application.settings.RowStyle
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Which column means what. The mapping is positional, so it is exactly the kind of thing that survives
 * a compiler and breaks a screen.
 */
@TestApplication
class TokenTableTest {
    private val row =
        ThemeEditorRow(
            section = SegmentKind.PREFIX,
            key = "hover",
            style = RowStyle("#4ee585", bold = true, enabled = true),
            origin = RowOrigin.INHERITED,
        )

    private var restyled: RowStyle? = null

    @Test
    fun `the columns read the row they say they do`() {
        val model = runInEdtAndGet { table().component.model }

        assertEquals("Prefix", model.getValueAt(0, 0))
        assertEquals("hover", model.getValueAt(0, 1))
        assertEquals("#4ee585", model.getValueAt(0, 2))
        assertEquals(true, model.getValueAt(0, 3))
        assertEquals(true, model.getValueAt(0, 4))
    }

    @Test
    fun `switching bold off restyles the row rather than the one beside it`() {
        val model = runInEdtAndGet { table().component.model }

        model.setValueAt(false, 0, 3)

        assertEquals(RowStyle("#4ee585", bold = false, enabled = true), restyled)
    }

    @Test
    fun `switching a row off keeps its colour`() {
        val model = runInEdtAndGet { table().component.model }

        model.setValueAt(false, 0, 4)

        assertEquals(RowStyle("#4ee585", bold = true, enabled = false), restyled)
    }

    @Test
    fun `a colour the parser cannot read is refused rather than stored`() {
        val model = runInEdtAndGet { table().component.model }

        model.setValueAt("not a colour", 0, 2)

        assertNull(restyled, "an unparseable hex value must not reach the theme")
    }

    @Test
    fun `a colour written without its hash is accepted`() {
        val model = runInEdtAndGet { table().component.model }

        model.setValueAt("abcdef", 0, 2)

        assertEquals("#abcdef", restyled?.color)
    }

    private fun table() = TokenTable(rowsOf = { listOf(row) }, restyle = { _, style -> restyled = style })
}
