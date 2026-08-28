package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
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
        FormBuilder.createFormBuilder()
            .addComponent(ownedByProject)
            .addComponentToRightColumn(JBLabel("Stored with the project, so a repository can share them"))
            .addLabeledComponent(JBLabel("Maximum file size:"), maxFileSize)
            .addLabeledComponent(JBLabel("Class identifiers:"), classIdentifiers)
            .addLabeledComponent(JBLabel("Class functions:"), classFunctions)
            .addLabeledComponent(JBLabel("Template tags:"), templateTags)
            .addLabeledComponent(JBLabel("Ignored prefix modifiers:"), ignoredModifiers)
            .addLabeledComponent(JBLabel("Supported file extensions:"), supportedExtensions)
            .panel

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
