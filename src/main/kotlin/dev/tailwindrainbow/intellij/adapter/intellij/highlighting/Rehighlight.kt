package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.project.ProjectManager

internal fun rehighlightOpenProjects() {
    ProjectManager.getInstance().openProjects.forEach { project ->
        DaemonCodeAnalyzer.getInstance(project).restart()
    }
}
