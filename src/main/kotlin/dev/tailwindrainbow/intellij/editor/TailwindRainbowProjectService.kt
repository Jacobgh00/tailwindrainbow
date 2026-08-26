package dev.tailwindrainbow.intellij.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.util.concurrency.AppExecutorUtil
import dev.tailwindrainbow.intellij.domain.HighlightSegment
import dev.tailwindrainbow.intellij.domain.RainbowThemes
import dev.tailwindrainbow.intellij.domain.TailwindDocumentScanner
import dev.tailwindrainbow.intellij.settings.TailwindRainbowSettings
import java.awt.Color
import java.awt.Font
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service(Service.Level.PROJECT)
class TailwindRainbowProjectService(private val project: Project) : Disposable {
    private val scanner = TailwindDocumentScanner()
    private val executor = AppExecutorUtil.createBoundedScheduledExecutorService("Tailwind Rainbow", 1)
    private val scheduledUpdates = ConcurrentHashMap<Editor, ScheduledFuture<*>>()
    private val highlighters = ConcurrentHashMap<Editor, List<RangeHighlighter>>()

    private val documentListener = object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            editorsFor(event.document).forEach { schedule(it, UPDATE_DELAY_MILLISECONDS) }
        }
    }

    private val editorFactoryListener = object : EditorFactoryListener {
        override fun editorCreated(event: EditorFactoryEvent) {
            if (event.editor.project == project) schedule(event.editor, 0)
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            clear(event.editor)
        }
    }

    init {
        val editorFactory = EditorFactory.getInstance()
        editorFactory.eventMulticaster.addDocumentListener(documentListener, this)
        editorFactory.addEditorFactoryListener(editorFactoryListener, this)
        refreshAllEditors()
    }

    fun refreshAllEditors() {
        editorsForProject().forEach { schedule(it, 0) }
    }

    private fun schedule(editor: Editor, delayMilliseconds: Long) {
        if (project.isDisposed || editor.isDisposed || editor.project != project) return

        scheduledUpdates.remove(editor)?.cancel(false)
        scheduledUpdates[editor] = executor.schedule(
            { calculateHighlights(editor) },
            delayMilliseconds,
            TimeUnit.MILLISECONDS,
        )
    }

    private fun calculateHighlights(editor: Editor) {
        scheduledUpdates.remove(editor)

        val request = ReadAction.compute<HighlightRequest?, RuntimeException> {
            if (project.isDisposed || editor.isDisposed) {
                return@compute null
            }

            val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return@compute null
            val snapshot = TailwindRainbowSettings.getInstance().snapshot()
            if (!snapshot.enabled) return@compute HighlightRequest(editor.document.modificationStamp, emptyList())

            val extension = file.extension ?: return@compute HighlightRequest(editor.document.modificationStamp, emptyList())
            val text = editor.document.immutableCharSequence.toString()
            val segments = scanner.scan(
                text = text,
                fileExtension = extension,
                settings = snapshot.scanSettings,
                theme = RainbowThemes.find(snapshot.themeName),
            )

            HighlightRequest(editor.document.modificationStamp, segments)
        } ?: return

        ApplicationManager.getApplication().invokeLater {
            if (!project.isDisposed && !editor.isDisposed && editor.document.modificationStamp == request.modificationStamp) {
                applyHighlights(editor, request.segments)
            }
        }
    }

    private fun applyHighlights(editor: Editor, segments: List<HighlightSegment>) {
        clearHighlighters(editor)

        highlighters[editor] = segments.map { segment ->
            editor.markupModel.addRangeHighlighter(
                segment.start,
                segment.end,
                HighlighterLayer.ADDITIONAL_SYNTAX,
                segment.toTextAttributes(),
                HighlighterTargetArea.EXACT_RANGE,
            )
        }
    }

    private fun clear(editor: Editor) {
        scheduledUpdates.remove(editor)?.cancel(false)
        clearHighlighters(editor)
    }

    private fun clearHighlighters(editor: Editor) {
        highlighters.remove(editor)?.forEach(editor.markupModel::removeHighlighter)
    }

    private fun editorsFor(document: Document): List<Editor> =
        EditorFactory.getInstance().allEditors.filter { it.project == project && it.document == document }

    private fun editorsForProject(): List<Editor> =
        EditorFactory.getInstance().allEditors.filter { it.project == project }

    override fun dispose() {
        scheduledUpdates.values.forEach { it.cancel(false) }
        scheduledUpdates.clear()
        editorsForProject().forEach(::clearHighlighters)
        executor.shutdownNow()
    }

    private data class HighlightRequest(
        val modificationStamp: Long,
        val segments: List<HighlightSegment>,
    )

    private companion object {
        const val UPDATE_DELAY_MILLISECONDS = 250L
    }
}

private fun HighlightSegment.toTextAttributes(): TextAttributes = TextAttributes().apply {
    foregroundColor = Color.decode(style.color)
    fontType = if (style.fontWeight.value >= 600) Font.BOLD else Font.PLAIN
}
