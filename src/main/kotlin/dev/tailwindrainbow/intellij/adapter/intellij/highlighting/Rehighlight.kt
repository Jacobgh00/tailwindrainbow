package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer
import com.intellij.openapi.project.ProjectManager

/**
 * Asks the platform to run highlighting again, everywhere it is open.
 *
 * The plugin does not own the markup — an annotator produces it and the daemon decides when — so a
 * change to what the colours should be is published by restarting the daemon rather than by
 * repainting anything.
 */
internal fun rehighlightOpenProjects() {
    ProjectManager.getInstance().openProjects.forEach { project ->
        DaemonCodeAnalyzer.getInstance(project).restart()
    }
}
