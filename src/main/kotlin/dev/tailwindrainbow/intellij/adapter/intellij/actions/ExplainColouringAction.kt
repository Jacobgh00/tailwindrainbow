package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.highlight.HighlightingSnapshot
import dev.tailwindrainbow.intellij.application.highlight.segmentAt
import dev.tailwindrainbow.intellij.application.port.HighlightDocument
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment

class ExplainColouringAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible =
            event.project != null && event.getData(CommonDataKeys.EDITOR) != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val extension = event.getData(CommonDataKeys.VIRTUAL_FILE)?.extension ?: return
        val document = editor.document
        val highlighting = PluginComponents.highlightingSnapshot(project)
        val snapshot =
            ExplanationSnapshot(
                text = document.text,
                fileExtension = extension,
                caretOffset = editor.caretModel.offset,
                documentStamp = document.modificationStamp,
                highlighting = highlighting,
            )

        object : Task.Backgroundable(project, message("explain.progress"), true) {
            private var explained: HighlightSegment? = null

            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                explained = snapshot.explainedBy(PluginComponents.highlightDocument(snapshot.highlighting))
            }

            override fun onSuccess() {
                if (!canShow(project, editor, snapshot)) return

                show(
                    editor,
                    explained?.let {
                        message("explain.matched", it.themeKey, it.kind.displayName, snapshot.highlighting.themeName)
                    },
                )
            }
        }.queue()
    }

    private fun canShow(
        project: Project,
        editor: Editor,
        snapshot: ExplanationSnapshot,
    ): Boolean {
        if (project.isDisposed || editor.isDisposed) return false

        val currentHighlighting = PluginComponents.highlightingSnapshot(project)

        return snapshot.isCurrent(
            documentStamp = editor.document.modificationStamp,
            caretOffset = editor.caretModel.offset,
            highlighting = currentHighlighting,
        )
    }

    private fun show(
        editor: Editor,
        explanation: String?,
    ) {
        HintManager.getInstance().showInformationHint(editor, explanation ?: message("explain.nothing"))
    }
}

internal data class ExplanationSnapshot(
    val text: String,
    val fileExtension: String,
    val caretOffset: Int,
    val documentStamp: Long,
    val highlighting: HighlightingSnapshot,
) {
    fun explainedBy(highlighter: HighlightDocument): HighlightSegment? =
        segmentAt(
            highlighter.highlight(text, fileExtension),
            caretOffset,
        )

    fun isCurrent(
        documentStamp: Long,
        caretOffset: Int,
        highlighting: HighlightingSnapshot,
    ): Boolean =
        this.documentStamp == documentStamp &&
            this.caretOffset == caretOffset &&
            this.highlighting == highlighting
}
