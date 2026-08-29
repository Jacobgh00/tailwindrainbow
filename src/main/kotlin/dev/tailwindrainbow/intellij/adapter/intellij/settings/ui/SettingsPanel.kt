package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.SettingsForm
import dev.tailwindrainbow.intellij.application.settings.ThemeWorkspace
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

class SettingsPanel(
    private val baseNames: List<String>,
    themeNames: List<String>,
    private val basePalette: (String) -> RainbowTheme,
    declaredVariants: () -> Set<String>,
) {
    private val themeEditor = ThemeEditorPanel(declaredVariants)
    private val enabled = JBCheckBox(message("settings.enable"))
    private val themeNameModel = MutableCollectionComboBoxModel(themeNames.toMutableList())
    private val theme = ComboBox(themeNameModel)
    private val menu = ThemeMenu(Commands())
    private val problems =
        ThemeProblemsBanner(
            onShow = { problem ->
                theme.selectedItem = problem.themeName
                themeEditor.select(problem.section, problem.key)
            },
            onRemove = { found -> show(parked().withoutEntriesFor(found), selectedThemeName()) },
        )
    private val recognition = RecognitionPanel()

    private var workspace = ThemeWorkspace()

    val component: DialogPanel =
        panel {
            row { cell(enabled) }
            row(message("settings.theme")) {
                cell(theme)
                cell(menu.component)
            }
            collapsibleGroup(message("settings.recognition.title"), indent = false) {
                row { cell(recognition.component).align(Align.FILL) }
            }.expanded = false
            separator()
            row { cell(problems.component).align(AlignX.FILL) }
            row { cell(themeEditor.component).align(Align.FILL) }.resizableRow()
        }

    init {
        theme.addActionListener { showSelectedTheme() }
    }

    fun showProblems(found: List<ThemeProblem>) = problems.show(found)

    fun read(): SettingsForm =
        SettingsForm(
            enabled = enabled.isSelected,
            themeName = selectedThemeName(),
            recognition = recognition.applicationRules(),
            projectRecognition = recognition.projectRules(),
            themes = parked().themes,
        )

    fun showStoredRecognition(form: SettingsForm) {
        recognition.show(form.recognition, form.projectRecognition)
    }

    fun write(form: SettingsForm) {
        enabled.isSelected = form.enabled
        recognition.show(form.recognition, form.projectRecognition)

        show(ThemeWorkspace(form.themes), form.themeName)
    }

    private fun selectedThemeName(): String = theme.selectedItem as? String ?: ""

    private fun parked(): ThemeWorkspace.Parked = workspace.holding(edited())

    private fun edited(): ThemeSpec? =
        workspace.editing
            .takeIf { it.isNotEmpty() }
            ?.let { themeEditor.specFor(it, workspace.baseOf(it)) }

    private fun showSelectedTheme() {
        workspace = parked().selecting(selectedThemeName())

        themeEditor.show(basePalette(workspace.baseOf(workspace.editing)), workspace.specFor(workspace.editing))
    }

    private fun offerNames() {
        themeNameModel.replaceAll(baseNames + workspace.themes.map(ThemeSpec::name).filterNot { it in baseNames })
    }

    private fun show(
        updated: ThemeWorkspace,
        selected: String,
    ) {
        workspace = updated
        offerNames()
        theme.selectedItem = selected
        showSelectedTheme()
    }

    private inner class Commands : ThemeCommands {
        override fun create() {
            val dialog = NewThemeDialog(baseNames) { themeNameModel.items.contains(it) }
            if (!dialog.showAndGet()) return

            show(parked().creating(dialog.enteredName, dialog.selectedBase), dialog.enteredName)
        }

        override fun duplicate() {
            val source = selectedThemeName()
            val dialog =
                named(
                    message("dialog.duplicateTheme.title"),
                    message("dialog.duplicateTheme.ok"),
                    message("dialog.duplicateTheme.suggested", source),
                )
            if (!dialog.showAndGet()) return

            show(parked().duplicating(source, dialog.enteredName), dialog.enteredName)
        }

        override fun rename() {
            val from = selectedThemeName()
            if (!ownsSelected()) return

            val dialog = named(message("dialog.renameTheme.title"), message("dialog.renameTheme.ok"), from)
            if (!dialog.showAndGet()) return

            show(parked().renaming(from, dialog.enteredName), dialog.enteredName)
        }

        override fun delete() {
            if (!ownsSelected()) return

            show(parked().removing(selectedThemeName()), baseNames.first())
        }

        override fun import() {
            val imported = chooseThemesToImport(menu.component)
            if (imported.isEmpty()) return

            show(parked().merging(imported), imported.first().name)
        }

        override fun export() = exportTheme(menu.component, selectedThemeName(), themeEditor.palette)

        override fun ownsSelected(): Boolean = selectedThemeName() !in baseNames

        private fun named(
            dialogTitle: String,
            okText: String,
            suggested: String,
        ) = ThemeNameDialog(
            dialogTitle = dialogTitle,
            okText = okText,
            suggested = suggested,
            isTaken = { it != selectedThemeName() && themeNameModel.items.contains(it) },
        )
    }
}
