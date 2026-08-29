package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.themesFromFile
import dev.tailwindrainbow.intellij.application.theme.toThemeFile
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import javax.swing.JComponent

internal fun exportTheme(
    parent: JComponent,
    name: String,
    palette: RainbowTheme,
) {
    val descriptor =
        FileSaverDescriptor(
            message("themeFile.export"),
            message("themeFile.export.description"),
            "json",
        )
    val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, parent)

    dialog.save("$name.json")?.file?.writeText(palette.toThemeFile(name))
}

internal fun chooseThemesToImport(parent: JComponent): List<ThemeSpec> {
    val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("json")
    val file = FileChooser.chooseFile(descriptor, parent, null, null) ?: return emptyList()
    val imported = themesFromFile(VfsUtilCore.loadText(file)).filter { it.entries.isNotEmpty() }

    if (imported.isEmpty()) {
        Messages.showErrorDialog(parent, message("themeFile.unreadable"), message("themeFile.import"))
    }

    return imported
}
