package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.SettingsForm
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * The settings view: widgets and layout, nothing else.
 *
 * Knows how to show a [SettingsForm] and hand one back. It never validates, never persists, and
 * never touches the highlighter — that is the configurable's job. Keeping it this dumb is what
 * makes the theme-editing table a matter of adding a row here rather than growing the presenter.
 */
class SettingsPanel(themeNames: List<String>) {
    private val enabled = JBCheckBox("Enable Tailwind Rainbow")
    private val theme = JComboBox(CollectionComboBoxModel(themeNames))
    private val maxFileSize = JBTextField()
    private val classIdentifiers = JBTextField()
    private val classFunctions = JBTextField()
    private val templateTags = JBTextField()
    private val ignoredModifiers = JBTextField()
    private val supportedExtensions = JBTextField()

    val component: JComponent = FormBuilder.createFormBuilder()
        .addComponent(enabled)
        .addLabeledComponent(JBLabel("Theme:"), theme)
        .addLabeledComponent(JBLabel("Maximum file size:"), maxFileSize)
        .addSeparator()
        .addLabeledComponent(JBLabel("Class identifiers:"), classIdentifiers)
        .addLabeledComponent(JBLabel("Class functions:"), classFunctions)
        .addLabeledComponent(JBLabel("Template tags:"), templateTags)
        .addLabeledComponent(JBLabel("Ignored prefix modifiers:"), ignoredModifiers)
        .addLabeledComponent(JBLabel("Supported file extensions:"), supportedExtensions)
        .addComponentFillVertically(JPanel(), 0)
        .panel

    fun read(): SettingsForm = SettingsForm(
        enabled = enabled.isSelected,
        themeName = theme.selectedItem as? String ?: "",
        maxFileSize = maxFileSize.text,
        classIdentifiers = classIdentifiers.text,
        classFunctions = classFunctions.text,
        templateTags = templateTags.text,
        ignoredPrefixModifiers = ignoredModifiers.text,
        supportedExtensions = supportedExtensions.text,
    )

    fun write(form: SettingsForm) {
        enabled.isSelected = form.enabled
        theme.selectedItem = form.themeName
        maxFileSize.text = form.maxFileSize
        classIdentifiers.text = form.classIdentifiers
        classFunctions.text = form.classFunctions
        templateTags.text = form.templateTags
        ignoredModifiers.text = form.ignoredPrefixModifiers
        supportedExtensions.text = form.supportedExtensions
    }
}
