package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.fields.ExpandableTextField
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
import dev.tailwindrainbow.intellij.application.settings.RecognitionOwner
import dev.tailwindrainbow.intellij.application.settings.RecognitionWorkspace
import dev.tailwindrainbow.intellij.application.settings.classIdentifiersWarning
import dev.tailwindrainbow.intellij.application.settings.maxFileSizeProblem
import dev.tailwindrainbow.intellij.application.settings.supportedExtensionsWarning
import javax.swing.JComponent

internal class RecognitionPanel {
    private val ownedByProject = JBCheckBox(message("settings.recognition.project"))
    private val maxFileSize = JBTextField()
    private val classIdentifiers = listField()
    private val classFunctions = listField()
    private val templateTags = listField()
    private val ignoredModifiers = listField()
    private val supportedExtensions = listField()
    private val readsClassLikeStrings = JBCheckBox(message("settings.recognition.classLikeStrings"))

    private var workspace: RecognitionWorkspace? = null

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
            row(message("settings.recognition.extensions")) {
                cell(supportedExtensions)
                    .align(AlignX.FILL)
                    .validationOnInput { field -> supportedExtensionsWarning(field.text)?.let { warning(it) } }
            }
            row {
                cell(readsClassLikeStrings)
                    .comment(message("settings.recognition.classLikeStrings.comment"))
            }
        }

    init {
        ownedByProject.addActionListener { swap() }
    }

    fun applicationRules(): RecognitionForm = saveDisplayed().applicationRules()

    fun projectRules(): RecognitionForm? = saveDisplayed().projectRules()

    fun show(
        application: RecognitionForm,
        project: RecognitionForm?,
    ) {
        val loaded = RecognitionWorkspace.load(application, project)
        workspace = loaded
        write(loaded.displayed)
        ownedByProject.isSelected = loaded.owner == RecognitionOwner.PROJECT
    }

    private fun swap() {
        val selected = if (ownedByProject.isSelected) RecognitionOwner.PROJECT else RecognitionOwner.APPLICATION
        val selectedWorkspace = saveDisplayed().select(selected)
        workspace = selectedWorkspace
        write(selectedWorkspace.displayed)
    }

    private fun listField() =
        ExpandableTextField(
            { text -> text.split(SEPARATOR).map(String::trim).filter(String::isNotEmpty).toMutableList() },
            { values -> values.joinToString("$SEPARATOR ") },
        )

    private fun onScreen() =
        RecognitionForm(
            maxFileSize = maxFileSize.text,
            classIdentifiers = classIdentifiers.text,
            classFunctions = classFunctions.text,
            templateTags = templateTags.text,
            ignoredPrefixModifiers = ignoredModifiers.text,
            supportedExtensions = supportedExtensions.text,
            readsClassLikeStrings = readsClassLikeStrings.isSelected,
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
        readsClassLikeStrings.isSelected = rules.readsClassLikeStrings
    }

    private fun saveDisplayed(): RecognitionWorkspace {
        val current = onScreen()
        val saved = workspace?.updateDisplayed(current) ?: RecognitionWorkspace.load(current, null)
        workspace = saved
        return saved
    }
}
