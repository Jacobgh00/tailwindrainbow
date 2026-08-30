package dev.tailwindrainbow.intellij.adapter.theme.vscode

import dev.tailwindrainbow.intellij.application.port.ThemeFileCodec
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

internal object VsCodeThemeCodec : ThemeFileCodec {
    override val extension: String = "json"

    override fun read(text: String): List<ThemeSpec> = themesFromFile(text)

    override fun write(
        name: String,
        theme: RainbowTheme,
    ): String = theme.toThemeFile(name)
}
