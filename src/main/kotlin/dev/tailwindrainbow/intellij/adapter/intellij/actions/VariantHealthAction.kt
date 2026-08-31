package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.port.Cancellation
import dev.tailwindrainbow.intellij.application.variants.VariantHealthReport
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

class VariantHealthAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        event.project?.let(::showReport)
    }

    private fun showReport(project: Project) {
        object : Task.Backgroundable(project, message("variantHealth.progress"), true) {
            private lateinit var report: VariantHealthReport

            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                report =
                    PluginComponents.variantHealth(
                        project,
                        Cancellation(indicator::checkCanceled),
                    )
            }

            override fun onSuccess() {
                VariantHealthDialog(project, report) { showReport(project) }.show()
            }
        }.queue()
    }
}
