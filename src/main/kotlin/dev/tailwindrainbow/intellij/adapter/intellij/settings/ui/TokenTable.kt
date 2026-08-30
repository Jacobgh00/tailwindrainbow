package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.ColorChooserService
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.JBUI
import dev.tailwindrainbow.intellij.adapter.color.toHex
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.toTextAttributes
import dev.tailwindrainbow.intellij.application.settings.RowStyle
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.isHexColor
import dev.tailwindrainbow.intellij.domain.theme.toHexColorOrNull
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

internal class TokenTable(
    private val rowsOf: () -> List<ThemeEditorRow>,
    private val restyle: (ThemeEditorRow, RowStyle) -> Unit,
) {
    private val tableModel = RowTableModel()

    val component: JBTable =
        JBTable(tableModel).apply {
            rowHeight = JBUI.scale(ROW_HEIGHT)
            setShowGrid(false)
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            columnModel.getColumn(SECTION).maxWidth = JBUI.scale(SECTION_COLUMN_WIDTH)
            columnModel.getColumn(COLOR).cellRenderer = ColorSwatchRenderer()
            columnModel.getColumn(TOKEN).cellRenderer = TokenRenderer()
        }

    init {
        TableSpeedSearch.installOn(component)
        component.addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(event: MouseEvent) = chooseColourIfSwatchClicked(event)
            },
        )
    }

    fun refresh() = tableModel.fireTableDataChanged()

    fun selected(): ThemeEditorRow? = rowsOf().getOrNull(component.selectedRow)

    fun clearSelection() = component.clearSelection()

    fun select(index: Int) {
        component.selectionModel.setSelectionInterval(index, index)
        component.scrollRectToVisible(component.getCellRect(index, TOKEN, true))
    }

    private fun chooseColourIfSwatchClicked(event: MouseEvent) {
        val row = component.rowAtPoint(event.point)
        if (row < 0 || component.columnAtPoint(event.point) != COLOR || !overSwatch(row, event)) return

        val target = rowsOf().getOrNull(row) ?: return

        // The click also starts the cell's text editor; leaving it open would write the old hex back.
        component.cellEditor?.cancelCellEditing()

        val chosen =
            ColorChooserService.instance.showDialog(
                component,
                message("editor.colour.choose"),
                target.style.color.takeIf(String::isHexColor)?.let(Color::decode),
                false,
            ) ?: return

        restyle(target, target.style.copy(color = chosen.toHex()))
        refresh()
    }

    private fun overSwatch(
        row: Int,
        event: MouseEvent,
    ): Boolean {
        val cell = component.getCellRect(row, COLOR, true)

        return event.point.x - cell.x <= JBUI.scale(SWATCH_SIZE + SWATCH_MARGIN)
    }

    private inner class RowTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = rowsOf().size

        override fun getColumnCount(): Int = COLUMN_NAMES.size

        override fun getColumnName(column: Int): String = COLUMN_NAMES[column]

        override fun getColumnClass(column: Int): Class<*> = if (column in SWITCHES) BOOLEAN_COLUMN else TEXT_COLUMN

        override fun isCellEditable(
            row: Int,
            column: Int,
        ): Boolean = column in SWITCHES || column == COLOR

        override fun getValueAt(
            row: Int,
            column: Int,
        ): Any =
            rowsOf()[row].let {
                when (column) {
                    SECTION -> it.section.displayName
                    TOKEN -> it.label
                    COLOR -> it.style.color
                    BOLD -> it.style.bold
                    else -> it.style.enabled
                }
            }

        override fun setValueAt(
            value: Any,
            row: Int,
            column: Int,
        ) {
            val target = rowsOf()[row]
            val restyled =
                when (column) {
                    BOLD -> target.style.copy(bold = value as Boolean)
                    ENABLED -> target.style.copy(enabled = value as Boolean)
                    COLOR -> target.style.copy(color = value.toString().toHexColorOrNull() ?: return)
                    else -> return
                }

            restyle(target, restyled)
            fireTableRowsUpdated(row, row)
        }
    }

    private class ColorSwatchRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            selected: Boolean,
            focused: Boolean,
            row: Int,
            column: Int,
        ): Component =
            super.getTableCellRendererComponent(table, value, selected, focused, row, column).also {
                val swatch = (value as? String)?.takeIf(String::isHexColor)?.let(Color::decode)

                (it as? DefaultTableCellRenderer)?.icon = swatch?.let { colour -> ColorIcon(SWATCH_SIZE, colour) }
            }
    }

    private inner class TokenRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable,
            value: Any?,
            selected: Boolean,
            focused: Boolean,
            row: Int,
            column: Int,
        ): Component =
            super.getTableCellRendererComponent(table, value, selected, focused, row, column).also {
                val target = rowsOf().getOrNull(row) ?: return@also
                val background = if (selected) table.selectionBackground else table.background

                it.font = it.font.deriveFont(if (target.style.bold) Font.BOLD else Font.PLAIN)
                it.foreground = target.style.painted(background) ?: it.foreground
                it.isEnabled = target.style.enabled
            }
    }

    private companion object {
        val BOOLEAN_COLUMN: Class<*> = java.lang.Boolean::class.java
        val TEXT_COLUMN: Class<*> = String::class.java
        const val ROW_HEIGHT = 28
        const val SECTION_COLUMN_WIDTH = 90
        const val SWATCH_SIZE = 16
        const val SWATCH_MARGIN = 6
        const val SECTION = 0
        const val TOKEN = 1
        const val COLOR = 2
        const val BOLD = 3
        const val ENABLED = 4

        val COLUMN_NAMES
            get() =
                arrayOf(
                    message("editor.column.section"),
                    message("editor.column.token"),
                    message("editor.column.colour"),
                    message("editor.column.bold"),
                    message("editor.column.enabled"),
                )

        val SWITCHES = setOf(BOLD, ENABLED)

        fun RowStyle.painted(background: Color): Color? =
            color
                .takeIf(String::isHexColor)
                ?.let {
                    TextStyle(
                        it,
                        FontWeight.of(if (bold) FontWeight.BOLD.value else FontWeight.NORMAL.value),
                        enabled,
                    )
                }
                ?.toTextAttributes(background)
                ?.foregroundColor
    }
}
