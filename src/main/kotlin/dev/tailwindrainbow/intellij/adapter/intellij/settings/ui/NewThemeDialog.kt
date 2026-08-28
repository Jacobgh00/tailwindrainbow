package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
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
        title = "New Theme"
        setOKButtonText("Create")
        init()
    }

    override fun createCenterPanel(): JComponent =
        FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Name:"), name)
            .addLabeledComponent(JBLabel("Based on:"), base)
            .addComponentToRightColumn(JBLabel("Colours you do not change keep following the base theme"))
            .panel

    override fun getPreferredFocusedComponent(): JComponent = name

    override fun doValidate(): ValidationInfo? =
        when {
            enteredName.isEmpty() -> ValidationInfo("Enter a name", name)
            isTaken(enteredName) -> ValidationInfo("A theme called '$enteredName' already exists", name)
            else -> null
        }

    private companion object {
        const val NAME_FIELD_COLUMNS = 20
    }
}
