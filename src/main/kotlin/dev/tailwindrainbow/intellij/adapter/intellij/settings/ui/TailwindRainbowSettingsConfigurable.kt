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
 * Presenter for the settings screen.
 *
 * Owns the `isModified`/`apply`/`reset` lifecycle the IDE defines, and the side effects that go
 * with it: validation failures become [ConfigurationException], accepted settings are persisted,
 * and every open project is re-highlighted. The widgets live in [SettingsPanel] and the validation
 * lives in [SettingsFormMapper]; neither knows about the other.
 */
class TailwindRainbowSettingsConfigurable : SearchableConfigurable {
    private var panel: SettingsPanel? = null

    override fun getId(): String = "dev.tailwindrainbow.intellij.settings"

    override fun getDisplayName(): String = "Tailwind Rainbow"

    override fun createComponent(): JComponent {
        val settings = TailwindRainbowSettings.getInstance()
        val created = SettingsPanel(settings.themeRepository().names.toList())

        created.write(SettingsFormMapper.toForm(settings.current()))
        panel = created

        return created.component
    }

    /** An unparseable form counts as modified, so Apply runs and reports why. */
    override fun isModified(): Boolean = when (val result = currentResult() ?: return false) {
        is FormResult.Valid -> result.settings != TailwindRainbowSettings.getInstance().current()
        is FormResult.Invalid -> true
    }

    override fun apply() {
        when (val result = currentResult() ?: return) {
            is FormResult.Invalid -> throw ConfigurationException(result.message)
            is FormResult.Valid -> {
                TailwindRainbowSettings.getInstance().update(result.settings)
                ProjectManager.getInstance().openProjects.forEach { project ->
                    DaemonCodeAnalyzer.getInstance(project).restart()
                }
            }
        }
    }

    override fun reset() {
        panel?.write(SettingsFormMapper.toForm(TailwindRainbowSettings.getInstance().current()))
    }

    override fun disposeUIResources() {
        panel = null
    }

    private fun currentResult(): FormResult? = panel?.read()?.let(SettingsFormMapper::toSettings)
}
