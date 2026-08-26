package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorModel
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * Lets the user recolour the selected theme, one token per row.
 *
 * A view over [ThemeEditorModel]: the table asks the model what to show and hands every edit back to
 * it, so which colour wins and what gets stored is decided by tested logic, not by Swing.
 */
class ThemeEditorPanel : JPanel(BorderLayout()) {
    private var model = ThemeEditorModel(RainbowTheme())
    private var themeName = ""

    private val tableModel = RowTableModel()
    private val table =
        JBTable(tableModel).apply {
            rowHeight = JBUI.scale(ROW_HEIGHT)
            setShowGrid(false)
            columnModel.getColumn(COLOR).cellRenderer = ColorSwatchRenderer()
            columnModel.getColumn(TOKEN).cellRenderer = TokenRenderer()
        }

    private val colorPanel = ColorPanel()
    private val resetButton = JButton("Reset to inherited")

    init {
        add(JBLabel("Colours for the selected theme — pick a row, then choose a colour:"), BorderLayout.NORTH)
        add(JScrollPane(table), BorderLayout.CENTER)
        add(
            JPanel().apply {
                add(JBLabel("Colour:"))
                add(colorPanel)
                add(resetButton)
            },
            BorderLayout.SOUTH,
        )

        colorPanel.addActionListener { applySelectedColour() }
        resetButton.addActionListener { resetSelected() }
        table.selectionModel.addListSelectionListener { syncColourPicker() }
    }

    /** Points the editor at a theme: [inherited] is what the user sees before overriding anything. */
    fun show(
        name: String,
        inherited: RainbowTheme,
        overrides: ThemeSpec?,
    ) {
        themeName = name
        model = ThemeEditorModel(inherited, overrides)
        tableModel.fireTableDataChanged()
        syncColourPicker()
    }

    /** The overrides the user has made, or null when they have made none. */
    fun overrides(): ThemeSpec? = model.spec(themeName).takeIf { it.entries.isNotEmpty() }

    private fun selectedRow(): ThemeEditorRow? = model.rows().getOrNull(table.selectedRow)

    private fun applySelectedColour() {
        val row = selectedRow() ?: return
        val chosen = colorPanel.selectedColor ?: return

        model = model.recolour(row.section, row.key, chosen.toHex())
        tableModel.fireTableDataChanged()
    }

    private fun resetSelected() {
        val row = selectedRow() ?: return

        model = model.reset(row.section, row.key)
        tableModel.fireTableDataChanged()
        syncColourPicker()
    }

    private fun syncColourPicker() {
        val row = selectedRow()
        colorPanel.isEnabled = row != null
        resetButton.isEnabled = row?.overridden == true
        colorPanel.selectedColor = row?.color?.let(Color::decode)
    }

    private inner class RowTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = model.rows().size

        override fun getColumnCount(): Int = 3

        override fun getColumnName(column: Int): String = COLUMN_NAMES[column]

        override fun getColumnClass(column: Int): Class<*> = if (column == BOLD) java.lang.Boolean::class.java else String::class.java

        override fun isCellEditable(
            row: Int,
            column: Int,
        ): Boolean = column == BOLD

        override fun getValueAt(
            row: Int,
            column: Int,
        ): Any =
            model.rows()[row].let {
                when (column) {
                    TOKEN -> it.label
                    COLOR -> it.color
                    else -> it.bold
                }
            }

        override fun setValueAt(
            value: Any,
            row: Int,
            column: Int,
        ) {
            if (column != BOLD) return

            val target = model.rows()[row]
            model = model.setBold(target.section, target.key, value as Boolean)
            fireTableRowsUpdated(row, row)
        }
    }

    /** Paints the cell in the colour it holds, so the table reads as a palette. */
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
                it.background = (value as? String)?.let(Color::decode) ?: table.background
                it.foreground = it.background
            }
    }

    /** Marks overridden tokens so the user can see what they have changed. */
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
                val overridden = model.rows().getOrNull(row)?.overridden == true
                it.font = it.font.deriveFont(if (overridden) Font.BOLD else Font.PLAIN)
            }
    }

    private companion object {
        const val ROW_HEIGHT = 24
        const val TOKEN = 0
        const val COLOR = 1
        const val BOLD = 2
        val COLUMN_NAMES = arrayOf("Token", "Colour", "Bold")

        fun Color.toHex(): String = "#%02x%02x%02x".format(red, green, blue)
    }
}
