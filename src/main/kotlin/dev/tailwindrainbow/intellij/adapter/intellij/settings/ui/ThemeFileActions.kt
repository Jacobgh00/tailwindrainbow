package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.themeFromFile
import dev.tailwindrainbow.intellij.application.theme.toThemeFile
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import javax.swing.JComponent

internal fun exportThemeAction(
    parent: JComponent,
    themeName: () -> String,
    palette: () -> RainbowTheme,
): AnAction =
    object : DumbAwareAction(message("themeFile.export"), null, AllIcons.ToolbarDecorator.Export) {
        override fun actionPerformed(event: AnActionEvent) {
            val descriptor =
                FileSaverDescriptor(
                    message("themeFile.export"),
                    message("themeFile.export.description"),
                    "json",
                )
            val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, parent)
            val chosen = dialog.save("${themeName()}.json")

            chosen?.file?.writeText(palette().toThemeFile(themeName()))
        }
    }

internal fun importThemeAction(
    parent: JComponent,
    onImported: (ThemeSpec) -> Unit,
): AnAction =
    object : DumbAwareAction(message("themeFile.import"), null, AllIcons.ToolbarDecorator.Import) {
        override fun actionPerformed(event: AnActionEvent) {
            val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
            val file = FileChooser.chooseFile(descriptor, parent, null, null) ?: return
            val imported = themeFromFile(VfsUtilCore.loadText(file))

            if (imported == null) {
                Messages.showErrorDialog(parent, message("themeFile.unreadable"), message("themeFile.import"))
            } else {
                onImported(imported)
            }
        }
    }
