package dev.tailwindrainbow.intellij.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.domain.RainbowThemes
import dev.tailwindrainbow.intellij.domain.ScanSettings
import dev.tailwindrainbow.intellij.editor.TailwindRainbowProjectService
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel


class TailwindRainbowSettingsConfigurable : SearchableConfigurable {
    private val enabled = JBCheckBox("Enable Tailwind Rainbow")
    private val theme = JComboBox(CollectionComboBoxModel(RainbowThemes.names.toList()))
    private val maxFileSize = JBTextField()
    private val classIdentifiers = JBTextField()
    private val classFunctions = JBTextField()
    private val templateTags = JBTextField()
    private val ignoredModifiers = JBTextField()
    private val supportedExtensions = JBTextField()
    private var panel: JPanel? = null

    override fun getId(): String = "dev.tailwindrainbow.intellij.settings"

    override fun getDisplayName(): String = "Tailwind Rainbow"

    override fun createComponent(): JComponent = FormBuilder.createFormBuilder()
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
        .also { panel = it }

    override fun isModified(): Boolean = readForm() != TailwindRainbowSettings.getInstance().snapshot()

    override fun apply() {
        val parsedMaxFileSize = maxFileSize.text.toIntOrNull()
        if (parsedMaxFileSize == null || parsedMaxFileSize <= 0) {
            throw ConfigurationException("Maximum file size must be greater than zero")
        }

        val snapshot = readForm(parsedMaxFileSize)
        TailwindRainbowSettings.getInstance().update(snapshot)
        ProjectManager.getInstance().openProjects.forEach { project ->
            project.service<TailwindRainbowProjectService>().refreshAllEditors()
        }
    }

    override fun reset() {
        writeForm(TailwindRainbowSettings.getInstance().snapshot())
    }

    override fun disposeUIResources() {
        panel = null
    }

    private fun readForm(
        parsedMaxFileSize: Int = maxFileSize.text.toIntOrNull()?.takeIf { it > 0 } ?: 1,
    ): SettingsSnapshot {
        return SettingsSnapshot(
            enabled = enabled.isSelected,
            themeName = theme.selectedItem as? String ?: RainbowThemes.DEFAULT_NAME,
            scanSettings = ScanSettings(
                maxFileSize = parsedMaxFileSize,
                classIdentifiers = classIdentifiers.values(),
                classFunctions = classFunctions.values(),
                templateTags = templateTags.values(),
                ignoredPrefixModifiers = ignoredModifiers.values(),
                supportedExtensions = supportedExtensions.values().map(String::lowercase).toSet(),
            ),
        )
    }

    private fun writeForm(snapshot: SettingsSnapshot) {
        enabled.isSelected = snapshot.enabled
        theme.selectedItem = snapshot.themeName
        maxFileSize.text = snapshot.scanSettings.maxFileSize.toString()
        classIdentifiers.text = snapshot.scanSettings.classIdentifiers.joinToString(", ")
        classFunctions.text = snapshot.scanSettings.classFunctions.joinToString(", ")
        templateTags.text = snapshot.scanSettings.templateTags.joinToString(", ")
        ignoredModifiers.text = snapshot.scanSettings.ignoredPrefixModifiers.joinToString(", ")
        supportedExtensions.text = snapshot.scanSettings.supportedExtensions.joinToString(", ")
    }
}

private fun JBTextField.values(): Set<String> =
    text.split(',').map(String::trim).filter(String::isNotEmpty).toSet()
