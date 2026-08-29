package dev.tailwindrainbow.intellij.adapter.intellij.statusbar

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.adapter.intellij.actions.themeChooser
import dev.tailwindrainbow.intellij.adapter.intellij.scannedLength
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.ScanStatus
import dev.tailwindrainbow.intellij.application.highlight.statusFor
import dev.tailwindrainbow.intellij.bootstrap.PluginComponents

const val TAILWIND_STATUS_WIDGET_ID = "dev.tailwindrainbow.statusBar"

internal class TailwindStatusWidget(project: Project) : EditorBasedStatusBarPopup(project, false) {
    override fun ID(): String = TAILWIND_STATUS_WIDGET_ID

    override fun createInstance(project: Project): EditorBasedStatusBarPopup = TailwindStatusWidget(project)

    override fun createPopup(context: DataContext): ListPopup = themeChooser(TailwindRainbowSettings.getInstance())

    override fun getWidgetState(file: VirtualFile?): WidgetState {
        if (file == null) return WidgetState.HIDDEN

        val settings = PluginComponents.effectiveSettings(project)
        val status = settings.statusFor(file.extension.orEmpty(), file.scannedLength())

        return WidgetState(
            widgetTooltip(status, settings.themeName, file.extension.orEmpty(), settings.scan.maxFileSize),
            widgetText(status, settings.themeName),
            true,
        )
    }
}

internal fun widgetText(
    status: ScanStatus,
    themeName: String,
): String =
    when (status) {
        ScanStatus.DISABLED -> message("widget.text.disabled")
        ScanStatus.NOT_SUPPORTED, ScanStatus.TOO_LARGE -> message("widget.text.unscanned")
        ScanStatus.SCANNED -> message("widget.text.theme", themeName)
    }

internal fun widgetTooltip(
    status: ScanStatus,
    themeName: String,
    fileExtension: String,
    maxFileSize: Int,
): String =
    when (status) {
        ScanStatus.DISABLED -> message("widget.tooltip.disabled")
        ScanStatus.NOT_SUPPORTED -> message("widget.tooltip.notSupported", fileExtension)
        ScanStatus.TOO_LARGE -> message("widget.tooltip.tooLarge", maxFileSize)
        ScanStatus.SCANNED -> message("widget.tooltip.scanned", themeName)
    }
