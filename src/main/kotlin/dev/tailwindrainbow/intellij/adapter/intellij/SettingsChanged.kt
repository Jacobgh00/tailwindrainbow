package dev.tailwindrainbow.intellij.adapter.intellij

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import dev.tailwindrainbow.intellij.adapter.intellij.statusbar.TAILWIND_STATUS_WIDGET_ID

internal fun settingsChanged() {
    ProjectManager.getInstance().openProjects.forEach(::settingsChanged)
}

internal fun settingsChanged(project: Project) {
    ApplicationManager.getApplication().invokeLater({
        DaemonCodeAnalyzer.getInstance(project).restart()
        WindowManager.getInstance().getStatusBar(project)?.updateWidget(TAILWIND_STATUS_WIDGET_ID)
    }, ModalityState.any(), project.disposed)
}
