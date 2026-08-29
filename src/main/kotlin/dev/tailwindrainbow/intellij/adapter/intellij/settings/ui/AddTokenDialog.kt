package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import javax.swing.JComponent

internal class AddTokenDialog(
    private val isTaken: (SegmentKind, String) -> Boolean,
    private val declaredVariants: () -> Set<String>,
) : DialogWrapper(false) {
    private val sections = SegmentKind.entries.filter(SegmentKind::isKeyed)
    private val section = ComboBox(CollectionComboBoxModel(sections))
    private val suggestions = MutableCollectionComboBoxModel<String>()
    private val key = ComboBox(suggestions).apply { isEditable = true }
    private val suggestionsHint = JBLabel()

    val selectedSection: SegmentKind get() = section.item ?: SegmentKind.PREFIX
    val enteredKey: String get() = key.editor.item?.toString()?.trim().orEmpty()

    init {
        title = message("dialog.addToken.title")
        setOKButtonText(message("dialog.addToken.ok"))
        section.renderer = SimpleListCellRenderer.create("") { it.displayName }
        section.addActionListener { offerSuggestions() }
        init()
        offerSuggestions()
    }

    private fun offerSuggestions() {
        val typed = enteredKey
        val offered =
            if (selectedSection == SegmentKind.PREFIX) {
                declaredVariants().filterNot { isTaken(SegmentKind.PREFIX, it) }.sorted()
            } else {
                emptyList()
            }

        suggestions.replaceAll(offered)
        key.editor.item = typed

        suggestionsHint.isVisible = selectedSection == SegmentKind.PREFIX
        suggestionsHint.text =
            if (offered.isEmpty()) message("dialog.addToken.noSuggestions") else message("dialog.addToken.suggestions")
    }

    override fun createCenterPanel(): JComponent =
        panel {
            row(message("dialog.addToken.section")) { cell(section) }
            row(message("dialog.addToken.token")) {
                cell(key).comment(message("dialog.addToken.example"))
            }
            row("") { cell(suggestionsHint) }
        }

    override fun getPreferredFocusedComponent(): JComponent = key

    override fun doValidate(): ValidationInfo? =
        when {
            enteredKey.isEmpty() -> ValidationInfo(message("dialog.addToken.empty"), key)
            isTaken(selectedSection, enteredKey) ->
                ValidationInfo(message("dialog.addToken.duplicate", enteredKey, selectedSection.displayName), key)
            else -> null
        }

    private companion object {
        const val TOKEN_FIELD_COLUMNS = 20
    }
}
