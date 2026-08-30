package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import dev.tailwindrainbow.intellij.adapter.intellij.TailwindRainbowBundle.message
import dev.tailwindrainbow.intellij.application.port.ThemeFileCodec
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import javax.swing.JComponent

internal class ThemeFileActions(
    private val codec: ThemeFileCodec,
) {
    fun export(
        parent: JComponent,
        name: String,
        palette: RainbowTheme,
    ) {
        val descriptor =
            FileSaverDescriptor(
                message("themeFile.export"),
                message("themeFile.export.description"),
                codec.extension,
            )
        val dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, parent)

        dialog.save("$name.${codec.extension}")?.file?.writeText(codec.write(name, palette))
    }

    fun chooseThemes(parent: JComponent): List<ThemeSpec> {
        val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor(codec.extension)
        val file = FileChooser.chooseFile(descriptor, parent, null, null) ?: return emptyList()
        val imported = codec.read(VfsUtilCore.loadText(file)).filter { it.entries.isNotEmpty() }

        if (imported.isEmpty()) {
            Messages.showErrorDialog(parent, message("themeFile.unreadable"), message("themeFile.import"))
        }

        return imported
    }
}
