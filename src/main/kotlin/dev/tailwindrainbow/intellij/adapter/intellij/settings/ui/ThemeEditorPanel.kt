package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.SearchTextField
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.RowOrigin
import dev.tailwindrainbow.intellij.application.settings.RowStyle
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorModel
import dev.tailwindrainbow.intellij.application.settings.ThemeEditorRow
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class ThemeEditorPanel(private val declaredVariants: () -> Set<String>) {
    private var model = ThemeEditorModel(RainbowTheme())
    private var inherited = RainbowTheme()

    val palette: RainbowTheme get() = model.palette()

    private val sections = listOf<SegmentKind?>(null) + SegmentKind.entries

    private val sectionFilter =
        ComboBox(CollectionComboBoxModel(sections)).apply {
            renderer =
                SimpleListCellRenderer.create<SegmentKind?> { label, value, _ ->
                    label.text = value?.displayName ?: message("editor.filter.all")
                }
        }

    private val find = SearchTextField(false)

    private val table = TokenTable(rowsOf = { shownRows }, restyle = ::restyle)

    private val preview = ThemePreviewPane()

    private val shownRows: List<ThemeEditorRow> get() = model.rows(sectionFilter.item, find.text)

    val component: JComponent =
        panel {
            row {
                label(message("editor.header"))
                    .comment(message("editor.header.comment"))
            }
            row {
                label(message("editor.filter"))
                cell(sectionFilter)
                label(message("editor.find"))
                cell(find).align(AlignX.FILL)
            }
            row {
                cell(tableWithToolbar())
                    .align(Align.FILL)
                    .comment(message("editor.colour.formats"))
            }.resizableRow()
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
        sectionFilter.addActionListener { redraw() }
        find.addDocumentListener(
            object : DocumentAdapter() {
                override fun textChanged(event: DocumentEvent) = redraw()
            },
        )
    }

    fun show(
        inherited: RainbowTheme,
        overrides: ThemeSpec?,
    ) {
        this.inherited = inherited
        model = ThemeEditorModel(inherited, overrides)
        table.clearSelection()
        redraw()
    }

    fun specFor(
        name: String,
        basedOn: String,
    ): ThemeSpec? = model.spec(name).copy(basedOn = basedOn).takeIf { !it.isRedundant }

    fun select(
        section: SegmentKind,
        key: String,
    ) {
        sectionFilter.item = null
        find.text = ""

        val index = shownRows.indexOfFirst { it.section == section && it.key == key }
        if (index < 0) return

        table.select(index)
    }

    private fun tableWithToolbar(): JPanel =
        ToolbarDecorator.createDecorator(table.component)
            .setAddAction { addToken() }
            .setRemoveAction { removeSelectedToken() }
            .setRemoveActionUpdater { table.selected()?.origin == RowOrigin.ADDED }
            .addExtraAction(resetAction())
            .disableUpDownActions()
            .createPanel()

    private fun resetAction() =
        object : DumbAwareAction(message("editor.reset"), null, AllIcons.General.Reset) {
            override fun getActionUpdateThread() = ActionUpdateThread.EDT

            override fun update(event: AnActionEvent) {
                event.presentation.isEnabled = table.selected()?.origin == RowOrigin.OVERRIDDEN
            }

            override fun actionPerformed(event: AnActionEvent) {
                val row = table.selected() ?: return

                model = model.reset(row.section, row.key)
                redraw()
            }
        }

    private fun addToken() {
        val dialog = AddTokenDialog(model::holds, declaredVariants)
        if (!dialog.showAndGet()) return

        model = model.add(dialog.selectedSection, dialog.enteredKey)
        sectionFilter.item = null
        redraw()
        select(dialog.selectedSection, dialog.enteredKey)
    }

    private fun removeSelectedToken() {
        val row = table.selected() ?: return

        model = model.remove(row.section, row.key)
        table.clearSelection()
        redraw()
    }

    private fun restyle(
        row: ThemeEditorRow,
        style: RowStyle,
    ) {
        model = model.restyle(row.section, row.key, style)
        preview.show(model.palette())
    }

    private fun redraw() {
        table.refresh()
        preview.show(model.palette())
    }
}
