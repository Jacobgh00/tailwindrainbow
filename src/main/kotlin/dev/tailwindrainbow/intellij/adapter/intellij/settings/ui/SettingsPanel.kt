package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.SettingsForm
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Never validates, never persists, never touches the highlighter — that is the configurable's job.
 *
 * Owns which theme is being edited: the editor below colours a palette without knowing whose it is,
 * so the name and base travel with the spec on the way in and out.
 */
class SettingsPanel(
    private val builtInNames: List<String>,
    themeNames: List<String>,
    private val builtInTheme: (String) -> RainbowTheme,
) {
    private val themeEditor = ThemeEditorPanel()
    private val enabled = JBCheckBox("Enable Tailwind Rainbow")
    private val themeNameModel = MutableCollectionComboBoxModel(themeNames.toMutableList())
    private val theme = ComboBox(themeNameModel)
    private val newTheme = JButton("New…")
    private val deleteTheme = JButton("Delete")
    private val maxFileSize = JBTextField()
    private val classIdentifiers = JBTextField()
    private val classFunctions = JBTextField()
    private val templateTags = JBTextField()
    private val ignoredModifiers = JBTextField()
    private val supportedExtensions = JBTextField()

    /** The theme the editor is showing, which is not always the selected one — see [park]. */
    private var editing = ""
    private var themes: List<ThemeSpec> = emptyList()

    val component: JComponent =
        FormBuilder.createFormBuilder()
            .addComponent(enabled)
            .addLabeledComponent(JBLabel("Theme:"), themeChooser())
            .addLabeledComponent(JBLabel("Maximum file size:"), maxFileSize)
            .addSeparator()
            .addLabeledComponent(JBLabel("Class identifiers:"), classIdentifiers)
            .addLabeledComponent(JBLabel("Class functions:"), classFunctions)
            .addLabeledComponent(JBLabel("Template tags:"), templateTags)
            .addLabeledComponent(JBLabel("Ignored prefix modifiers:"), ignoredModifiers)
            .addLabeledComponent(JBLabel("Supported file extensions:"), supportedExtensions)
            .addSeparator()
            .addComponentFillVertically(themeEditor, 0)
            .panel

    init {
        theme.addActionListener { showSelectedTheme() }
        newTheme.addActionListener { createTheme() }
        deleteTheme.addActionListener { deleteSelectedTheme() }
    }

    fun read(): SettingsForm =
        SettingsForm(
            enabled = enabled.isSelected,
            themeName = selectedThemeName(),
            maxFileSize = maxFileSize.text,
            classIdentifiers = classIdentifiers.text,
            classFunctions = classFunctions.text,
            templateTags = templateTags.text,
            ignoredPrefixModifiers = ignoredModifiers.text,
            supportedExtensions = supportedExtensions.text,
            themes = park(),
        )

    fun write(form: SettingsForm) {
        enabled.isSelected = form.enabled
        maxFileSize.text = form.maxFileSize
        classIdentifiers.text = form.classIdentifiers
        classFunctions.text = form.classFunctions
        templateTags.text = form.templateTags
        ignoredModifiers.text = form.ignoredPrefixModifiers
        supportedExtensions.text = form.supportedExtensions

        themes = form.themes
        editing = ""
        themeNameModel.replaceAll(builtInNames + form.themes.map(ThemeSpec::name).filterNot { it in builtInNames })
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
        deleteTheme.isEnabled = editing !in builtInNames

        themeEditor.show(builtInTheme(baseOf(editing)), themes.firstOrNull { it.name == editing })
    }

    private fun createTheme() {
        val dialog = NewThemeDialog(builtInNames) { themeNameModel.items.contains(it) }
        if (!dialog.showAndGet()) return

        themes = park() + ThemeSpec(dialog.enteredName, emptyList(), basedOn = dialog.selectedBase)
        editing = ""
        themeNameModel.add(dialog.enteredName)
        theme.selectedItem = dialog.enteredName
    }

    private fun deleteSelectedTheme() {
        val name = selectedThemeName()
        if (name in builtInNames) return

        themes = themes.filterNot { it.name == name }
        editing = ""
        themeNameModel.remove(name)
        theme.selectedItem = builtInNames.first()
    }

    /** The theme a name derives its untouched colours from. Built-in themes derive from themselves. */
    private fun baseOf(name: String): String = themes.firstOrNull { it.name == name }?.basedOn ?: name

    private companion object {
        const val CHOOSER_GAP = 4
    }
}
