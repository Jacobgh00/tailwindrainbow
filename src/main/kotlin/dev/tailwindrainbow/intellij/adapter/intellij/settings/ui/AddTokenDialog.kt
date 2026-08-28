package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import javax.swing.JComponent

/**
 * Asks which token to colour. Only the two keyed sections are offered, because `arbitrary` and
 * `important` hold a single style each and are always present.
 *
 * [isTaken] is the editor's own view of what it already holds, so the dialog reports a duplicate
 * while the user types instead of letting the model reject it afterwards.
 */
internal class AddTokenDialog(
    private val isTaken: (SegmentKind, String) -> Boolean,
) : DialogWrapper(false) {
    private val sections = SegmentKind.entries.filter(SegmentKind::isKeyed)
    private val section = ComboBox(CollectionComboBoxModel(sections))
    private val key = JBTextField(TOKEN_FIELD_COLUMNS)

    val selectedSection: SegmentKind get() = section.item ?: SegmentKind.PREFIX
    val enteredKey: String get() = key.text.trim()

    init {
        title = "Add Token"
        setOKButtonText("Add")
        section.renderer = SimpleListCellRenderer.create("") { it.displayName }
        init()
    }

    override fun createCenterPanel(): JComponent =
        FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("Section:"), section)
            .addLabeledComponent(JBLabel("Token:"), key)
            .addComponentToRightColumn(JBLabel("For example hover, focus-visible, min-*, or bg-*"))
            .panel

    override fun getPreferredFocusedComponent(): JComponent = key

    override fun doValidate(): ValidationInfo? =
        when {
            enteredKey.isEmpty() -> ValidationInfo("Enter a token", key)
            isTaken(selectedSection, enteredKey) ->
                ValidationInfo("'$enteredKey' is already in the ${selectedSection.displayName} section", key)
            else -> null
        }

    private companion object {
        const val TOKEN_FIELD_COLUMNS = 20
    }
}
