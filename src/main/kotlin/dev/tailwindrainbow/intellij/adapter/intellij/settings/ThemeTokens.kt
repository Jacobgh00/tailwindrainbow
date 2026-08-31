package dev.tailwindrainbow.intellij.adapter.intellij.settings

import dev.tailwindrainbow.intellij.adapter.intellij.settingsChanged
import dev.tailwindrainbow.intellij.application.settings.addingEntry
import dev.tailwindrainbow.intellij.application.settings.newThemeEntry
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

fun addTokenToCurrentTheme(variant: String) {
    val settings = TailwindRainbowSettings.getInstance()
    val current = settings.current()

    settings.update(
        current,
        settings.themes.overrides().addingEntry(current.themeName, newThemeEntry(SegmentKind.PREFIX, variant)),
    )
    settingsChanged()
}
