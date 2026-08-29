package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.editor.colors.EditorColorsListener
import com.intellij.openapi.editor.colors.EditorColorsScheme
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settingsChanged

class EditorSchemeListener : EditorColorsListener {
    override fun globalSchemeChange(scheme: EditorColorsScheme?) {
        TailwindRainbowSettings.getInstance().reloadThemes()
        settingsChanged()
    }
}
