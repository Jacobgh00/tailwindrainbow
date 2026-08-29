package dev.tailwindrainbow.intellij.adapter.intellij.statusbar

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.actions.themeChooser
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.ScanStatus
import dev.tailwindrainbow.intellij.application.highlight.statusFor
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.settings.withProjectRecognition

const val TAILWIND_STATUS_WIDGET_ID = "dev.tailwindrainbow.statusBar"

internal class TailwindStatusWidget(project: Project) : EditorBasedStatusBarPopup(project, false) {
    override fun ID(): String = TAILWIND_STATUS_WIDGET_ID

    override fun createInstance(project: Project): EditorBasedStatusBarPopup = TailwindStatusWidget(project)

    override fun createPopup(context: DataContext): ListPopup = themeChooser(TailwindRainbowSettings.getInstance())

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        if (file == null) return WidgetState.HIDDEN

        val settings = effectiveSettings()
        val status = settings.statusFor(file.extension.orEmpty(), lengthOf(file))

        return WidgetState(tooltip(status, settings, file), text(status, settings), true)
    }

    private fun effectiveSettings(): HighlightSettings =
        TailwindRainbowSettings.getInstance()
            .current()
            .withProjectRecognition(TailwindRainbowProjectSettings.getInstance(project).recognition())

    private fun lengthOf(file: VirtualFile): Int =
        FileDocumentManager.getInstance().getCachedDocument(file)?.textLength ?: file.length.toInt()

    private fun text(
        status: ScanStatus,
        settings: HighlightSettings,
    ): String =
        when (status) {
            ScanStatus.DISABLED -> message("widget.text.disabled")
            ScanStatus.NOT_SUPPORTED, ScanStatus.TOO_LARGE -> message("widget.text.unscanned")
            ScanStatus.SCANNED -> message("widget.text.theme", settings.themeName)
        }

    private fun tooltip(
        status: ScanStatus,
        settings: HighlightSettings,
        file: VirtualFile,
    ): String =
        when (status) {
            ScanStatus.DISABLED -> message("widget.tooltip.disabled")
            ScanStatus.NOT_SUPPORTED -> message("widget.tooltip.notSupported", file.extension.orEmpty())
            ScanStatus.TOO_LARGE -> message("widget.tooltip.tooLarge", settings.scan.maxFileSize)
            ScanStatus.SCANNED -> message("widget.tooltip.scanned", settings.themeName)
        }
}
