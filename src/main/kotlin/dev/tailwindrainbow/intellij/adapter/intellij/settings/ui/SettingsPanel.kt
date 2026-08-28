package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.SettingsForm
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.describe
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import java.awt.FlowLayout
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.SwingConstants

/**
 * Never validates, never persists, never touches the highlighter — that is the configurable's job.
 *
 * Owns which theme is being edited: the editor below colours a palette without knowing whose it is,
 * so the name and base travel with the spec on the way in and out.
 */
class SettingsPanel(
    private val baseNames: List<String>,
    themeNames: List<String>,
    private val basePalette: (String) -> RainbowTheme,
    declaredVariants: () -> Set<String>,
) {
    private val themeEditor = ThemeEditorPanel(declaredVariants)
    private val enabled = JBCheckBox("Enable Tailwind Rainbow")
    private val themeNameModel = MutableCollectionComboBoxModel(themeNames.toMutableList())
    private val theme = ComboBox(themeNameModel)
    private val newTheme = JButton("New…")
    private val deleteTheme = JButton("Delete")
    private val problems = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }
    private val recognition = RecognitionPanel()

    /** The theme the editor is showing, which is not always the selected one — see [park]. */
    private var editing = ""
    private var themes: List<ThemeSpec> = emptyList()

    val component: JComponent =
        FormBuilder.createFormBuilder()
            .addComponent(enabled)
            .addLabeledComponent(JBLabel("Theme:"), themeChooser())
            .addSeparator()
            .addComponent(recognition.component)
            .addSeparator()
            .addComponent(problems)
            .addComponentFillVertically(themeEditor, 0)
            .panel

    init {
        theme.addActionListener { showSelectedTheme() }
        newTheme.addActionListener { createTheme() }
        deleteTheme.addActionListener { deleteSelectedTheme() }
    }

    /**
     * Lists entries the stored themes hold that the plugin cannot use.
     *
     * They are dropped when a theme is read, so without this the colour simply would not appear and
     * nothing would say why. Shown rather than thrown: the user did not necessarily cause them, and
     * the rest of the screen still works.
     */
    fun showProblems(found: List<ThemeProblem>) {
        problems.removeAll()
        found.forEach { problems.add(JBLabel(it.describe(), AllIcons.General.Warning, SwingConstants.LEFT)) }
        problems.isVisible = found.isNotEmpty()
        problems.revalidate()
        problems.repaint()
    }

    fun read(): SettingsForm =
        SettingsForm(
            enabled = enabled.isSelected,
            themeName = selectedThemeName(),
            recognition = recognition.applicationRules(),
            projectRecognition = recognition.projectRules(),
            themes = park(),
        )

    fun write(form: SettingsForm) {
        enabled.isSelected = form.enabled
        recognition.show(form.recognition, form.projectRecognition)

        themes = form.themes
        editing = ""
        themeNameModel.replaceAll(baseNames + form.themes.map(ThemeSpec::name).filterNot { it in baseNames })
        theme.selectedItem = form.themeName
        showSelectedTheme()
    }

    private fun themeChooser(): JPanel =
        JPanel(FlowLayout(FlowLayout.LEFT, CHOOSER_GAP, 0)).apply {
            add(theme)
            add(newTheme)
            add(deleteTheme)
        }

    private fun selectedThemeName(): String = theme.selectedItem as? String ?: ""

    /**
     * Every theme's overrides, with the ones being edited right now folded back in.
     *
     * Keyed on [editing] rather than on the selection, because when a selection changes the editor
     * still holds the theme that was left behind, and that is the one to keep.
     */
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

    /** The theme a name derives its untouched colours from. Built-in themes derive from themselves. */
    private fun baseOf(name: String): String = themes.firstOrNull { it.name == name }?.basedOn ?: name

    private companion object {
        const val CHOOSER_GAP = 4
    }
}
