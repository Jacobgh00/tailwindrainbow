package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

interface ThemeDefinitionSource {
    val sourceName: String

    fun specs(): List<ThemeSpec>
}
