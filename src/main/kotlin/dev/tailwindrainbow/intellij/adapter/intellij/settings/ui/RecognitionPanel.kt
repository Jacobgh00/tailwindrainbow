package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
import javax.swing.JComponent

/**
 * What the plugin looks at, and who decides it.
 *
 * One set of fields serves both answers: the switch says whether they belong to the project, and
 * the side not on screen waits in [parked] so turning the switch on and off again loses neither.
 */
internal class RecognitionPanel {
    private val ownedByProject = JBCheckBox("Use project settings for what is recognized")
    private val maxFileSize = JBTextField()
    private val classIdentifiers = JBTextField()
    private val classFunctions = JBTextField()
    private val templateTags = JBTextField()
    private val ignoredModifiers = JBTextField()
    private val supportedExtensions = JBTextField()

    private var parked: RecognitionForm? = null

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

    /** The rules to store IDE-wide, whichever side is on screen. */
    fun applicationRules(): RecognitionForm = if (ownedByProject.isSelected) parked ?: onScreen() else onScreen()

    /** The rules to store in the project, or null while it follows the IDE-wide ones. */
    fun projectRules(): RecognitionForm? = onScreen().takeIf { ownedByProject.isSelected }

    fun show(
        application: RecognitionForm,
        project: RecognitionForm?,
    ) {
        ownedByProject.isSelected = project != null
        write(project ?: application)
        parked = application.takeIf { project != null }
    }

    private fun swap() {
        val current = onScreen()

        write(parked ?: current)
        parked = current
    }

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
