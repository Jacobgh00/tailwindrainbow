package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.theme.ThemeHealthContext

fun interface ThemeHealthCatalog {
    fun themeHealthNamed(name: String): ThemeHealthContext
}
