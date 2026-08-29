package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings

class SelectThemeAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return

        themeChooser(TailwindRainbowSettings.getInstance()).showCenteredInCurrentWindow(project)
    }
}
