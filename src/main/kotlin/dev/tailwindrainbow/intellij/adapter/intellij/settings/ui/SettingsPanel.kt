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
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import javax.swing.JButton

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
    private val newTheme = JButton(message("settings.theme.new"))
    private val deleteTheme = JButton(message("settings.theme.delete"))
    private val problems =
        ThemeProblemsBanner(
            onShow = { problem ->
                theme.selectedItem = problem.themeName
                themeEditor.select(problem.section, problem.key)
            },
            onRemove = { found ->
                themes = themes.withoutEntriesFor(found)
                editing = ""
                showSelectedTheme()
            },
        )
    private val recognition = RecognitionPanel()

    private var editing = ""
    private var themes: List<ThemeSpec> = emptyList()

    val component: DialogPanel =
        panel {
            row { cell(enabled) }
            row(message("settings.theme")) {
                cell(theme)
                cell(newTheme)
                cell(deleteTheme)
            }
            separator()
            row { cell(recognition.component).align(Align.FILL) }
            separator()
            row { cell(problems.component).align(AlignX.FILL) }
            row { cell(themeEditor.component).align(Align.FILL) }.resizableRow()
        }

    init {
        theme.addActionListener { showSelectedTheme() }
        newTheme.addActionListener { createTheme() }
        deleteTheme.addActionListener { deleteSelectedTheme() }
    }

    fun showProblems(found: List<ThemeProblem>) = problems.show(found)

    fun read(): SettingsForm =
        SettingsForm(
            enabled = enabled.isSelected,
            themeName = selectedThemeName(),
            recognition = recognition.applicationRules(),
            projectRecognition = recognition.projectRules(),
            themes = park(),
        )

    fun showStoredRecognition(form: SettingsForm) {
        recognition.show(form.recognition, form.projectRecognition)
    }

    fun write(form: SettingsForm) {
        enabled.isSelected = form.enabled
        recognition.show(form.recognition, form.projectRecognition)

        themes = form.themes
        editing = ""
        themeNameModel.replaceAll(baseNames + form.themes.map(ThemeSpec::name).filterNot { it in baseNames })
        theme.selectedItem = form.themeName
        showSelectedTheme()
    }

    private fun selectedThemeName(): String = theme.selectedItem as? String ?: ""

    private fun park(): List<ThemeSpec> {
        if (editing.isEmpty()) return themes

        return themes.filterNot { it.name == editing } + listOfNotNull(themeEditor.specFor(editing, baseOf(editing)))
    }

    private fun showSelectedTheme() {
        themes = park()
        editing = selectedThemeName()
        deleteTheme.isEnabled = editing !in baseNames

        themeEditor.show(basePalette(baseOf(editing)), themes.firstOrNull { it.name == editing })
    }

    private fun createTheme() {
        val dialog = NewThemeDialog(baseNames) { themeNameModel.items.contains(it) }
        if (!dialog.showAndGet()) return

        themes = park() + ThemeSpec(dialog.enteredName, emptyList(), basedOn = dialog.selectedBase)
        editing = ""
        themeNameModel.add(dialog.enteredName)
        theme.selectedItem = dialog.enteredName
    }

    private fun deleteSelectedTheme() {
        val name = selectedThemeName()
        if (name in baseNames) return

        themes = themes.filterNot { it.name == name }
        editing = ""
        themeNameModel.remove(name)
        theme.selectedItem = baseNames.first()
    }

    private fun baseOf(name: String): String = themes.firstOrNull { it.name == name }?.basedOn ?: name

    private companion object {
        const val CHOOSER_GAP = 4
    }
}

private fun List<ThemeSpec>.withoutEntriesFor(problems: List<ThemeProblem>): List<ThemeSpec> =
    map { spec ->
        spec.copy(
            entries =
                spec.entries.filterNot { entry ->
                    problems.any { it.themeName == spec.name && it.section == entry.section && it.key == entry.key }
                },
        )
    }
