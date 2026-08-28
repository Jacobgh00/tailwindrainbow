package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.ProjectManager
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.settings.FormResult
import dev.tailwindrainbow.intellij.application.settings.SettingsFormMapper
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.describe
import dev.tailwindrainbow.intellij.application.theme.problemsIntroducedBy
import javax.swing.JComponent

/**
 * The widgets live in [SettingsPanel] and the validation in [SettingsFormMapper]; this owns only
 * the IDE's `isModified`/`apply`/`reset` lifecycle and the side effects that go with it.
 */
class TailwindRainbowSettingsConfigurable : SearchableConfigurable {
    private var panel: SettingsPanel? = null

    override fun getId(): String = "dev.tailwindrainbow.intellij.settings"

    override fun getDisplayName(): String = "Tailwind Rainbow"

    override fun createComponent(): JComponent {
        val settings = TailwindRainbowSettings.getInstance()
        val created =
            SettingsPanel(
                baseNames = settings.themes.baseNames().toList(),
                themeNames = settings.themes.names().toList(),
                basePalette = { name -> settings.themes.basePalette(name) },
            )

        created.write(SettingsFormMapper.toForm(settings.current(), settings.themes.overrides()))
        created.showProblems(settings.themes.problems())
        panel = created

        return created.component
    }

    /** An unparseable form counts as modified, so Apply runs and reports why. */
    override fun isModified(): Boolean =
        when (val result = currentResult() ?: return false) {
            is FormResult.Valid ->
                result.settings != TailwindRainbowSettings.getInstance().current() ||
                    result.themes != TailwindRainbowSettings.getInstance().themes.overrides()
            is FormResult.Invalid -> true
        }

    override fun apply() {
        when (val result = currentResult() ?: return) {
            is FormResult.Invalid -> throw ConfigurationException(result.message)
            is FormResult.Valid -> {
                val settings = TailwindRainbowSettings.getInstance()
                refuseIntroducedProblems(result.themes, settings.themes.overrides())

                settings.update(result.settings, result.themes)
                panel?.showProblems(settings.themes.problems())
                ProjectManager.getInstance().openProjects.forEach { project ->
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    /**
     * Entries already stored broken are listed rather than refused, so unrelated changes can still
     * be saved; only what this edit would add is worth stopping.
     */
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
        val settings = TailwindRainbowSettings.getInstance()
        panel?.write(SettingsFormMapper.toForm(settings.current(), settings.themes.overrides()))
        panel?.showProblems(settings.themes.problems())
    }

    override fun disposeUIResources() {
        panel = null
    }

    private fun currentResult(): FormResult? = panel?.read()?.let(SettingsFormMapper::toSettings)
}
