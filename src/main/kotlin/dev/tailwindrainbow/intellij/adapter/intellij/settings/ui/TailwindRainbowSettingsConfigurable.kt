package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.ProjectManager
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.settings.FormResult
import dev.tailwindrainbow.intellij.application.settings.SettingsFormMapper
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
                themeNames = settings.themes.names().toList(),
                inheritedTheme = { name -> settings.themes.inherited(name) },
            )

        created.write(SettingsFormMapper.toForm(settings.current(), settings.themes.overrides()))
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
                TailwindRainbowSettings.getInstance().update(result.settings, result.themes)
                ProjectManager.getInstance().openProjects.forEach { project ->
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    override fun reset() {
        val settings = TailwindRainbowSettings.getInstance()
        panel?.write(SettingsFormMapper.toForm(settings.current(), settings.themes.overrides()))
    }

    override fun disposeUIResources() {
        panel = null
    }

    private fun currentResult(): FormResult? = panel?.read()?.let(SettingsFormMapper::toSettings)
}
