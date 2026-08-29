package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import dev.tailwindrainbow.intellij.adapter.intellij.scannedExtension
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

class TailwindRainbowAnnotator : Annotator {
    override fun annotate(
        element: PsiElement,
        holder: AnnotationHolder,
    ) {
        if (element !is PsiFile) {
            return
        }

        val extension = element.scannedExtension() ?: return
        val background = EditorColorsManager.getInstance().globalScheme.defaultBackground

        PluginComponents.highlightDocument(element.project).highlight(element.text, extension).forEach { segment ->
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                .range(TextRange(segment.start, segment.end))
                .enforcedTextAttributes(segment.style.toTextAttributes(background))
                .create()
        }
    }
}
