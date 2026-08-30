package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

interface ThemeFileCodec {
    val extension: String

    fun read(text: String): List<ThemeSpec>

    fun write(
        name: String,
        theme: RainbowTheme,
    ): String
}
