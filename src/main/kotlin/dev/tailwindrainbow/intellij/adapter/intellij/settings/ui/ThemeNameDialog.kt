package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import javax.swing.JComponent

internal class ThemeNameDialog(
    dialogTitle: String,
    okText: String,
    suggested: String,
    private val isTaken: (String) -> Boolean,
) : DialogWrapper(false) {
    private val name = JBTextField(suggested, NAME_FIELD_COLUMNS)

    val enteredName: String get() = name.text.trim()

    init {
        title = dialogTitle
        setOKButtonText(okText)
        init()
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row(message("dialog.newTheme.name")) { cell(name) }
        }

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
