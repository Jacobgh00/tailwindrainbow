package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.addTokenToCurrentTheme
import dev.tailwindrainbow.intellij.application.variants.VariantAssessment
import dev.tailwindrainbow.intellij.application.variants.VariantDeclaration
import dev.tailwindrainbow.intellij.application.variants.VariantHealthReport
import dev.tailwindrainbow.intellij.application.variants.VariantStatus
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

internal class VariantHealthDialog(
    private val project: Project,
    private val report: VariantHealthReport,
    private val refresh: () -> Unit,
) : DialogWrapper(false) {
    private val assessments =
        JBList(report.assessments).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            cellRenderer =
                SimpleListCellRenderer.create<VariantAssessment> { label, value, _ ->
                    label.text = value.describe()
                }
        }
    private val details =
        JBTextArea().apply {
            isEditable = false
            lineWrap = true
            wrapStyleWord = true
            rows = DETAILS_ROWS
        }
    private val addButton = JButton(message("variantHealth.addColour"))
    private val openButton = JButton(message("variantHealth.openDeclaration"))
    private val refreshButton = JButton(message("variantHealth.refresh"))

    private val declarationSelector =
        ComboBox(CollectionComboBoxModel<VariantDeclaration>(emptyList())).apply {
            renderer =
                SimpleListCellRenderer.create<VariantDeclaration> { label, value, _ ->
                    label.text = value.describe()
                }
            addActionListener { updateOpenButton() }
        }

    init {
        title = message("variantHealth.title")
        setOKButtonText(message("variantHealth.close"))
        assessments.addListSelectionListener { updateSelection() }
        addButton.addActionListener { repairSelected() }
        openButton.addActionListener { openSelectedDeclaration() }
        refreshButton.addActionListener {
            close(CANCEL_EXIT_CODE)
            refresh()
        }
        init()
        if (assessments.model.size > 0) assessments.selectedIndex = 0
        updateSelection()
    }

    override fun createCenterPanel(): JComponent {
        val root = JPanel(BorderLayout(GAP, GAP))
        root.preferredSize = Dimension(WIDTH, HEIGHT)
        root.add(summary(), BorderLayout.NORTH)

        val lower = JPanel(BorderLayout(GAP, GAP))
        lower.add(declarationPicker(), BorderLayout.NORTH)
        lower.add(JBScrollPane(details), BorderLayout.CENTER)
        lower.add(buttons(), BorderLayout.SOUTH)

        root.add(JBScrollPane(assessments), BorderLayout.CENTER)
        root.add(lower, BorderLayout.SOUTH)
        return root
    }

    override fun getPreferredFocusedComponent(): JComponent = assessments

    private fun summary(): JComponent =
        JBLabel(
            message(
                "variantHealth.summary",
                report.assessments.size,
                report.problems.size,
                report.theme.name,
                report.scan.scannedFileCount,
                scanCaveats(),
            ),
        )

    private fun scanCaveats(): String =
        buildString {
            report.scan.oversizedFileCount
                .takeIf { it > 0 }
                ?.let { append(message("variantHealth.scan.oversized", it)) }

            if (report.scan.reachedFileLimit) append(message("variantHealth.scan.limit"))
        }

    private fun buttons(): JComponent =
        JPanel(FlowLayout(FlowLayout.LEADING, GAP, 0)).apply {
            add(addButton)
            add(openButton)
            add(refreshButton)
        }

    private fun declarationPicker(): JComponent =
        JPanel(BorderLayout(GAP, GAP)).apply {
            add(JBLabel(message("variantHealth.declarationChoice")), BorderLayout.WEST)
            add(declarationSelector, BorderLayout.CENTER)
        }

    private fun updateSelection() {
        val selected = assessments.selectedValue
        declarationSelector.model = CollectionComboBoxModel(selected?.declarations.orEmpty())
        declarationSelector.isEnabled = selected?.declarations?.isNotEmpty() == true
        addButton.isEnabled = selected?.status is VariantStatus.MissingColour
        updateOpenButton()
        details.text = selected?.describeDetails(report) ?: message("variantHealth.noSelection")
    }

    private fun updateOpenButton() {
        val selectedDeclaration = declarationSelector.selectedItem as? VariantDeclaration
        openButton.isEnabled = selectedDeclaration?.let(::canOpen) == true
    }

    private fun repairSelected() {
        val selected = assessments.selectedValue ?: return
        if (selected.status !is VariantStatus.MissingColour) return

        addTokenToCurrentTheme(selected.name)
        close(OK_EXIT_CODE)
    }

    private fun openSelectedDeclaration() {
        val declaration = declarationSelector.selectedItem as? VariantDeclaration ?: return
        openVariantDeclaration(project, declaration)
    }

    private companion object {
        const val GAP = 8
        const val WIDTH = 820
        const val HEIGHT = 540
        const val DETAILS_ROWS = 6
    }
}

internal fun openVariantDeclaration(
    project: Project,
    declaration: VariantDeclaration,
): Boolean {
    val location = declaration.location ?: return false
    val file = LocalFileSystem.getInstance().findFileByPath(location.path) ?: return false

    OpenFileDescriptor(project, file, location.startOffset).navigate(true)
    return true
}
