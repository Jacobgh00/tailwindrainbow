package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import dev.tailwindrainbow.intellij.adapter.intellij.highlighting.rehighlightOpenProjects
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings

/**
 * Switches the active theme from a chooser, without opening settings.
 *
 * Arrowing through the list previews the theme in the editor; the preview is transient, so leaving
 * the popup by any route other than choosing leaves the stored theme as it was.
 */
class SelectThemeAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val settings = TailwindRainbowSettings.getInstance()

        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(settings.themes.names().toList())
            .setTitle("Tailwind Rainbow Theme")
            .setSelectedValue(settings.current().themeName, true)
            .setItemSelectedCallback { name -> preview(settings, name) }
            .setItemChosenCallback { name -> choose(settings, name) }
            .addListener(cancellation(settings))
            .createPopup()
            .showCenteredInCurrentWindow(project)
    }

    private fun preview(
        settings: TailwindRainbowSettings,
        name: String?,
    ) {
        settings.previewTheme(name)
        rehighlightOpenProjects()
    }

    private fun choose(
        settings: TailwindRainbowSettings,
        name: String,
    ) {
        settings.chooseTheme(name)
        rehighlightOpenProjects()
    }

    /** Whatever else closes the popup — Escape, a click elsewhere — drops the preview. */
    private fun cancellation(settings: TailwindRainbowSettings) =
        object : JBPopupListener {
            override fun onClosed(event: LightweightWindowEvent) {
                if (!event.isOk) preview(settings, null)
            }
        }
}
