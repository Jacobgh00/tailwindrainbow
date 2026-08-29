package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import javax.swing.JComponent

internal class NewThemeDialog(
    private val bases: List<String>,
    private val isTaken: (String) -> Boolean,
) : DialogWrapper(false) {
    private val name = JBTextField(NAME_FIELD_COLUMNS)
    private val base = ComboBox(CollectionComboBoxModel(bases))

    val enteredName: String get() = name.text.trim()
    val selectedBase: String get() = base.item ?: bases.first()

    init {
        title = message("dialog.newTheme.title")
        setOKButtonText(message("dialog.newTheme.ok"))
        init()
    }

    override fun createCenterPanel(): JComponent =
        FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel(message("dialog.newTheme.name")), name)
            .addLabeledComponent(JBLabel(message("dialog.newTheme.base")), base)
            .addComponentToRightColumn(JBLabel(message("dialog.newTheme.comment")))
            .panel

    override fun getPreferredFocusedComponent(): JComponent = name

    override fun doValidate(): ValidationInfo? =
        when {
            enteredName.isEmpty() -> ValidationInfo(message("dialog.newTheme.empty"), name)
            isTaken(enteredName) -> ValidationInfo(message("dialog.newTheme.duplicate", enteredName), name)
            else -> null
        }

    private companion object {
        const val NAME_FIELD_COLUMNS = 20
    }
}
