package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.ColorPanel
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.tailwindrainbow.intellij.application.settings.RowOrigin
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorModel
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.isHexColor
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

/**
 * The table asks [ThemeEditorModel] what to show and hands every edit back to it, so which colour
 * wins and what gets stored is decided by tested logic rather than by Swing.
 */
class ThemeEditorPanel(private val declaredVariants: () -> Set<String>) : JPanel(BorderLayout()) {
    private var model = ThemeEditorModel(RainbowTheme())

    private val tableModel = RowTableModel()
    private val table =
        JBTable(tableModel).apply {
            rowHeight = JBUI.scale(ROW_HEIGHT)
            setShowGrid(false)
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            columnModel.getColumn(SECTION).maxWidth = JBUI.scale(SECTION_COLUMN_WIDTH)
            columnModel.getColumn(COLOR).cellRenderer = ColorSwatchRenderer()
            columnModel.getColumn(TOKEN).cellRenderer = TokenRenderer()
        }

    private val colorPanel = ColorPanel()
    private val resetButton = JButton("Reset to inherited")

    init {
        add(JBLabel("Colours for the selected theme — pick a row, then choose a colour:"), BorderLayout.NORTH)
        add(tableWithToolbar(), BorderLayout.CENTER)
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
        table.selectionModel.addListSelectionListener { syncControls() }
    }

    fun show(
        inherited: RainbowTheme,
        overrides: ThemeSpec?,
    ) {
        model = ThemeEditorModel(inherited, overrides)
        table.clearSelection()
        tableModel.fireTableDataChanged()
        syncControls()
    }

    /**
     * What the user has changed, named. The editor colours a palette and does not track which theme
     * it belongs to; [SettingsPanel] owns that, and passes the identity back in here.
     */
    fun specFor(
        name: String,
        basedOn: String,
    ): ThemeSpec? = model.spec(name).copy(basedOn = basedOn).takeIf { !it.isRedundant }

    /** Add and remove live on the table's own toolbar, where the platform puts them. */
    private fun tableWithToolbar(): JPanel =
        ToolbarDecorator.createDecorator(table)
            .setAddAction { addToken() }
            .setRemoveAction { removeSelectedToken() }
            .setRemoveActionUpdater { selectedRow()?.origin == RowOrigin.ADDED }
            .disableUpDownActions()
            .createPanel()

    private fun selectedRow(): ThemeEditorRow? = model.rows().getOrNull(table.selectedRow)

    private fun addToken() {
        val dialog = AddTokenDialog(model::holds, declaredVariants)
        if (!dialog.showAndGet()) return

        model = model.add(dialog.selectedSection, dialog.enteredKey)
        tableModel.fireTableDataChanged()
        select(dialog.selectedSection, dialog.enteredKey)
    }

    private fun removeSelectedToken() {
        val row = selectedRow() ?: return

        model = model.remove(row.section, row.key)
        table.clearSelection()
        tableModel.fireTableDataChanged()
        syncControls()
    }

    private fun applySelectedColour() {
        val row = selectedRow() ?: return
        val chosen = colorPanel.selectedColor ?: return

        model = model.restyle(row.section, row.key, row.style.copy(color = chosen.toHex()))
        tableModel.fireTableDataChanged()
    }

    private fun resetSelected() {
        val row = selectedRow() ?: return

        model = model.reset(row.section, row.key)
        tableModel.fireTableDataChanged()
        syncControls()
    }

    /** Puts the caret on a freshly added token so the colour picker acts on it straight away. */
    private fun select(
        section: SegmentKind,
        key: String,
    ) {
        val index = model.rows().indexOfFirst { it.section == section && it.key == key }
        if (index < 0) return

        table.selectionModel.setSelectionInterval(index, index)
        table.scrollRectToVisible(table.getCellRect(index, TOKEN, true))
    }

    private fun syncControls() {
        val row = selectedRow()
        colorPanel.isEnabled = row != null
        resetButton.isEnabled = row?.origin == RowOrigin.OVERRIDDEN
        colorPanel.selectedColor = row?.style?.color?.takeIf(String::isHexColor)?.let(Color::decode)
    }

    private inner class RowTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = model.rows().size

        override fun getColumnCount(): Int = COLUMN_NAMES.size

        override fun getColumnName(column: Int): String = COLUMN_NAMES[column]

        override fun getColumnClass(column: Int): Class<*> = if (column in SWITCHES) BOOLEAN_COLUMN else TEXT_COLUMN

        override fun isCellEditable(
            row: Int,
            column: Int,
        ): Boolean = column in SWITCHES

        override fun getValueAt(
            row: Int,
            column: Int,
        ): Any =
            model.rows()[row].let {
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
            val target = model.rows()[row]
            val restyled =
                when (column) {
                    BOLD -> target.style.copy(bold = value as Boolean)
                    ENABLED -> target.style.copy(enabled = value as Boolean)
                    else -> return
                }

            model = model.restyle(target.section, target.key, restyled)
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
                it.background = (value as? String)?.takeIf(String::isHexColor)?.let(Color::decode) ?: table.background
                it.foreground = it.background
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
                val target = model.rows().getOrNull(row)
                it.font = it.font.deriveFont(if (target?.isUserDefined == true) Font.BOLD else Font.PLAIN)
                it.isEnabled = target?.style?.enabled != false
            }
    }

    private companion object {
        val BOOLEAN_COLUMN: Class<*> = java.lang.Boolean::class.java
        val TEXT_COLUMN: Class<*> = String::class.java
        const val ROW_HEIGHT = 24
        const val SECTION_COLUMN_WIDTH = 90
        const val SECTION = 0
        const val TOKEN = 1
        const val COLOR = 2
        const val BOLD = 3
        const val ENABLED = 4
        val COLUMN_NAMES = arrayOf("Section", "Token", "Colour", "Bold", "Enabled")

        /** The columns the user toggles rather than types into. */
        val SWITCHES = setOf(BOLD, ENABLED)

        fun Color.toHex(): String = "#%02x%02x%02x".format(red, green, blue)
    }
}
