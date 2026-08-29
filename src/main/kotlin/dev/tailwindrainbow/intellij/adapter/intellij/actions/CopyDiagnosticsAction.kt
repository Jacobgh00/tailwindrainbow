package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.diagnostics.report
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents
import java.awt.datatransfer.StringSelection

const val TAILWIND_NOTIFICATIONS = "Tailwind Rainbow"

class CopyDiagnosticsAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)

        val report = PluginComponents.diagnostics(project, file).report()

        CopyPasteManager.getInstance().setContents(StringSelection(report))
        announce(project)
    }

    private fun announce(project: Project) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(TAILWIND_NOTIFICATIONS)
            .createNotification(message("diagnostics.copied"), NotificationType.INFORMATION)
            .notify(project)
    }
}
