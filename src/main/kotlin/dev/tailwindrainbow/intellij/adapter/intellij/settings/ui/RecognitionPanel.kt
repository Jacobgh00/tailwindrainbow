package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
import dev.tailwindrainbow.intellij.application.settings.classIdentifiersWarning
import dev.tailwindrainbow.intellij.application.settings.maxFileSizeProblem
import javax.swing.JComponent

internal class RecognitionPanel {
    private val ownedByProject = JBCheckBox(message("settings.recognition.project"))
    private val maxFileSize = JBTextField()
    private val classIdentifiers = listField()
    private val classFunctions = listField()
    private val templateTags = listField()
    private val ignoredModifiers = listField()
    private val supportedExtensions = listField()

    private var rulesOffScreen: RecognitionForm? = null

    val component: JComponent =
        panel {
            row {
                cell(ownedByProject)
                    .comment(message("settings.recognition.project.comment"))
            }
            row(message("settings.recognition.maxFileSize")) {
                cell(maxFileSize)
                    .align(AlignX.FILL)
                    .validationOnInput { field -> maxFileSizeProblem(field.text)?.let { error(it) } }
            }
            row(message("settings.recognition.classIdentifiers")) {
                cell(classIdentifiers)
                    .align(AlignX.FILL)
                    .validationOnInput { field -> classIdentifiersWarning(field.text)?.let { warning(it) } }
            }
            row(message("settings.recognition.classFunctions")) { cell(classFunctions).align(AlignX.FILL) }
            row(message("settings.recognition.templateTags")) { cell(templateTags).align(AlignX.FILL) }
            row(message("settings.recognition.ignoredModifiers")) { cell(ignoredModifiers).align(AlignX.FILL) }
            row(message("settings.recognition.extensions")) { cell(supportedExtensions).align(AlignX.FILL) }
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

    private fun listField() =
        ExpandableTextField(
            { text -> text.split(SEPARATOR).map(String::trim).filter(String::isNotEmpty).toMutableList() },
            { values -> values.joinToString("$SEPARATOR ") },
        )

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

    private companion object {
        const val SEPARATOR = ","
    }

    private fun write(rules: RecognitionForm) {
        maxFileSize.text = rules.maxFileSize
        classIdentifiers.text = rules.classIdentifiers
        classFunctions.text = rules.classFunctions
        templateTags.text = rules.templateTags
        ignoredModifiers.text = rules.ignoredPrefixModifiers
        supportedExtensions.text = rules.supportedExtensions
    }
}
