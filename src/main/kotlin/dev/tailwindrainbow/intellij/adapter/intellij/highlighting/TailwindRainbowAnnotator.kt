package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

/**
 * Registered for [com.intellij.lang.Language.ANY] (`language=""`), because the scanner reads raw
 * text and needs no grammar. Running inside the daemon's annotation pass means the platform owns
 * scheduling, debouncing, cancellation, and highlighter lifetime.
 */
class TailwindRainbowAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder,
    ) {
        if (element !is PsiFile) {
            return
        }

        val extension = element.virtualFile?.extension ?: return
        val background = EditorColorsManager.getInstance().globalScheme.defaultBackground

        PluginComponents.highlightDocument(element.project).highlight(element.text, extension).forEach { segment ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(segment.start, segment.end))
                .enforcedTextAttributes(segment.style.toTextAttributes(background))
                .create()
        }
    }
}
