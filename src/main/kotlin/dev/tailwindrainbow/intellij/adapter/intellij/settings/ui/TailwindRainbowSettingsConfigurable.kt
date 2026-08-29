package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.rehighlightOpenProjects
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.variants.ProjectVariants
import dev.tailwindrainbow.intellij.application.settings.FormResult
import dev.tailwindrainbow.intellij.application.settings.SettingsFormMapper
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.describe
import dev.tailwindrainbow.intellij.application.theme.problemsIntroducedBy
import javax.swing.JComponent

class TailwindRainbowSettingsConfigurable(private val project: Project) : SearchableConfigurable {
    private var panel: SettingsPanel? = null
    private var validators: Disposable? = null

    private val forProject get() = TailwindRainbowProjectSettings.getInstance(project)

    override fun getId(): String = "dev.tailwindrainbow.intellij.settings"

    override fun getDisplayName(): String = "Tailwind Rainbow"

    override fun createComponent(): JComponent {
        val settings = TailwindRainbowSettings.getInstance()
        val created =
            SettingsPanel(
                baseNames = settings.themes.baseNames().toList(),
                themeNames = settings.themes.names().toList(),
                basePalette = { name -> settings.themes.basePalette(name) },
                declaredVariants = { ProjectVariants.getInstance(project).declared() },
            )

        created.write(currentForm())
        created.showProblems(settings.themes.problems())
        panel = created
        validators = Disposer.newDisposable("TailwindRainbowFieldValidators")
        created.component.registerValidators(validators!!)

        return created.component
    }

    override fun isModified(): Boolean =
        when (val result = currentResult() ?: return false) {
            is FormResult.Valid ->
                result.settings != TailwindRainbowSettings.getInstance().current() ||
                    result.themes != TailwindRainbowSettings.getInstance().themes.overrides() ||
                    result.projectScan != forProject.recognition()
            is FormResult.Invalid -> true
        }

    override fun apply() {
        when (val result = currentResult() ?: return) {
            is FormResult.Invalid -> throw ConfigurationException(result.message)
            is FormResult.Valid -> {
                val settings = TailwindRainbowSettings.getInstance()
                refuseIntroducedProblems(result.themes, settings.themes.overrides())

                settings.update(result.settings, result.themes)
                forProject.update(result.projectScan)
                panel?.showStoredRecognition(currentForm())
                panel?.showProblems(settings.themes.problems())
                rehighlightOpenProjects()
            }
        }
    }

    private fun refuseIntroducedProblems(
        pending: List<ThemeSpec>,
        stored: List<ThemeSpec>,
    ) {
        val introduced = problemsIntroducedBy(pending, stored)

        if (introduced.isNotEmpty()) {
            throw ConfigurationException(introduced.joinToString("\n", transform = ThemeProblem::describe))
        }
    }

    override fun reset() {
        panel?.write(currentForm())
        panel?.showProblems(TailwindRainbowSettings.getInstance().themes.problems())
    }

    private fun currentForm() =
        TailwindRainbowSettings.getInstance().let { settings ->
            SettingsFormMapper.toForm(settings.current(), settings.themes.overrides(), forProject.recognition())
        }

    override fun disposeUIResources() {
        validators?.let(Disposer::dispose)
        validators = null
        panel = null
    }

    private fun currentResult(): FormResult? = panel?.read()?.let(SettingsFormMapper::toSettings)
}
