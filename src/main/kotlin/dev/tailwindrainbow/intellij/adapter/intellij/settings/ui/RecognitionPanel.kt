package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
import dev.tailwindrainbow.intellij.application.settings.classIdentifiersWarning
import dev.tailwindrainbow.intellij.application.settings.maxFileSizeProblem
import javax.swing.JComponent

internal class RecognitionPanel {
    private val ownedByProject = JBCheckBox("Use project settings for what is recognized")
    private val maxFileSize = JBTextField()
    private val classIdentifiers = JBTextField()
    private val classFunctions = JBTextField()
    private val templateTags = JBTextField()
    private val ignoredModifiers = JBTextField()
    private val supportedExtensions = JBTextField()

    private var rulesOffScreen: RecognitionForm? = null

    val component: JComponent =
        panel {
            row {
                cell(ownedByProject)
                    .comment("Stored with the project, so a repository can share them")
            }
            row("Maximum file size:") {
                cell(maxFileSize)
                    .align(AlignX.FILL)
                    .validationOnInput { field -> maxFileSizeProblem(field.text)?.let { error(it) } }
            }
            row("Class identifiers:") {
                cell(classIdentifiers)
                    .align(AlignX.FILL)
                    .validationOnInput { field -> classIdentifiersWarning(field.text)?.let { warning(it) } }
            }
            row("Class functions:") { cell(classFunctions).align(AlignX.FILL) }
            row("Template tags:") { cell(templateTags).align(AlignX.FILL) }
            row("Ignored prefix modifiers:") { cell(ignoredModifiers).align(AlignX.FILL) }
            row("Supported file extensions:") { cell(supportedExtensions).align(AlignX.FILL) }
        }

    init {
        ownedByProject.addActionListener { swap() }
    }

    fun applicationRules(): RecognitionForm = if (ownedByProject.isSelected) rulesOffScreen() else onScreen()

    fun projectRules(): RecognitionForm? = onScreen().takeIf { ownedByProject.isSelected }

    fun show(
        application: RecognitionForm,
        project: RecognitionForm?,
    ) {
        ownedByProject.isSelected = project != null
        write(project ?: application)
        rulesOffScreen = application.takeIf { project != null }
    }

    private fun swap() {
        val current = onScreen()

        write(rulesOffScreen ?: current)
        rulesOffScreen = current
    }

    private fun rulesOffScreen() = rulesOffScreen ?: onScreen()

    private fun onScreen() =
        RecognitionForm(
            maxFileSize = maxFileSize.text,
            classIdentifiers = classIdentifiers.text,
            classFunctions = classFunctions.text,
            templateTags = templateTags.text,
            ignoredPrefixModifiers = ignoredModifiers.text,
            supportedExtensions = supportedExtensions.text,
        )

    private fun write(rules: RecognitionForm) {
        maxFileSize.text = rules.maxFileSize
        classIdentifiers.text = rules.classIdentifiers
        classFunctions.text = rules.classFunctions
        templateTags.text = rules.templateTags
        ignoredModifiers.text = rules.ignoredPrefixModifiers
        supportedExtensions.text = rules.supportedExtensions
    }
}
