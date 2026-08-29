package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import dev.tailwindrainbow.intellij.adapter.intellij.statusbar.TAILWIND_STATUS_WIDGET_ID

internal fun settingsChanged() {
    ProjectManager.getInstance().openProjects.forEach { project ->
        DaemonCodeAnalyzer.getInstance(project).restart()
        WindowManager.getInstance().getStatusBar(project)?.updateWidget(TAILWIND_STATUS_WIDGET_ID)
    }
}
