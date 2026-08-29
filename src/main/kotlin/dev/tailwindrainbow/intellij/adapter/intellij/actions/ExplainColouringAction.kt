package dev.tailwindrainbow.intellij.adapter.intellij.actions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.segmentAt
import dev.tailwindrainbow.intellij.application.settings.displayName
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

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

        val segments = PluginComponents.highlightDocument(project).highlight(editor.document.text, extension)
        val explained = segmentAt(segments, editor.caretModel.offset)
        val themeName = TailwindRainbowSettings.getInstance().current().themeName

        show(editor, explained?.let { message("explain.matched", it.themeKey, it.kind.displayName, themeName) })
    }

    private fun show(
        editor: Editor,
        explanation: String?,
    ) {
        HintManager.getInstance().showInformationHint(editor, explanation ?: message("explain.nothing"))
    }
}
