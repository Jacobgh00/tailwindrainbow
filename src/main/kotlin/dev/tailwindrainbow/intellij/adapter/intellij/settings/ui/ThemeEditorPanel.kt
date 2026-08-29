package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.ColorPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.TableSpeedSearch
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.JBUI
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.RowOrigin
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorModel
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.isHexColor
import dev.tailwindrainbow.intellij.domain.theme.toHexColorOrNull
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class ThemeEditorPanel(private val declaredVariants: () -> Set<String>) {
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

    private val sections = listOf<SegmentKind?>(null) + SegmentKind.entries

    private val sectionFilter =
        ComboBox(CollectionComboBoxModel(sections)).apply {
            renderer = SimpleListCellRenderer.create("") { it?.displayName ?: message("editor.filter.all") }
        }

    private val preview = ThemePreviewPane()

    private val shownRows: List<ThemeEditorRow> get() = model.rows(sectionFilter.item)
    private val colorPanel = ColorPanel()

    val component: JComponent =
        panel {
            row {
                label(message("editor.header"))
                    .comment(message("editor.header.comment"))
            }
            row {
                label(message("editor.filter"))
                cell(sectionFilter)
            }
            row { cell(tableWithToolbar()).align(Align.FILL) }.resizableRow()
            row {
                label(message("editor.colour"))
                cell(colorPanel).comment(message("editor.colour.formats"))
            }
            row {
                cell(preview.component)
                    .align(AlignX.FILL)
                    .comment(message("editor.preview.comment"))
            }
            row {
                link(message("editor.preview.restore")) { preview.restoreSample() }
            }
        }

    init {
        TableSpeedSearch.installOn(table)
        sectionFilter.addActionListener { tableModel.fireTableDataChanged() }
        tableModel.addTableModelListener { preview.show(model.palette()) }
        colorPanel.addActionListener { applySelectedColour() }
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

    fun specFor(
        name: String,
        basedOn: String,
    ): ThemeSpec? = model.spec(name).copy(basedOn = basedOn).takeIf { !it.isRedundant }

    private fun tableWithToolbar(): JPanel =
        ToolbarDecorator.createDecorator(table)
            .setAddAction { addToken() }
            .setRemoveAction { removeSelectedToken() }
            .setRemoveActionUpdater { selectedRow()?.origin == RowOrigin.ADDED }
            .addExtraAction(resetAction())
            .disableUpDownActions()
            .createPanel()

    private fun resetAction() =
        object : DumbAwareAction(message("editor.reset"), null, AllIcons.General.Reset) {
            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(event: AnActionEvent) {
                event.presentation.isEnabled = selectedRow()?.origin == RowOrigin.OVERRIDDEN
            }

            override fun actionPerformed(event: AnActionEvent) {
                val row = selectedRow() ?: return

                model = model.reset(row.section, row.key)
                tableModel.fireTableDataChanged()
                syncControls()
            }
        }

    private fun selectedRow(): ThemeEditorRow? = shownRows.getOrNull(table.selectedRow)

    private fun addToken() {
        val dialog = AddTokenDialog(model::holds, declaredVariants)
        if (!dialog.showAndGet()) return

        model = model.add(dialog.selectedSection, dialog.enteredKey)
        sectionFilter.item = null
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

    fun select(
        section: SegmentKind,
        key: String,
    ) {
        sectionFilter.item = null

        val index = shownRows.indexOfFirst { it.section == section && it.key == key }
        if (index < 0) return

        table.selectionModel.setSelectionInterval(index, index)
        table.scrollRectToVisible(table.getCellRect(index, TOKEN, true))
    }

    private fun syncControls() {
        val row = selectedRow()
        colorPanel.isEnabled = row != null
        colorPanel.selectedColor = row?.style?.color?.takeIf(String::isHexColor)?.let(Color::decode)
    }

    private inner class RowTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = shownRows.size

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
            shownRows[row].let {
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
            val target = shownRows[row]
            val restyled =
                when (column) {
                    BOLD -> target.style.copy(bold = value as Boolean)
                    ENABLED -> target.style.copy(enabled = value as Boolean)
                    COLOR -> target.style.copy(color = value.toString().toHexColorOrNull() ?: return)
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
                val target = shownRows.getOrNull(row)
                it.font = it.font.deriveFont(if (target?.isUserDefined == true) Font.BOLD else Font.PLAIN)
                it.isEnabled = target?.style?.enabled != false
            }
    }

    private companion object {
        val BOOLEAN_COLUMN: Class<*> = java.lang.Boolean::class.java
        val TEXT_COLUMN: Class<*> = String::class.java
        const val ROW_HEIGHT = 24
        const val SECTION_COLUMN_WIDTH = 90
        const val SWATCH_SIZE = 12
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

        fun Color.toHex(): String = "#%02x%02x%02x".format(red, green, blue)
    }
}
