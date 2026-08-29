package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndWait
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@TestApplication
class EditorSchemeRefreshTest {
    private val manager = EditorColorsManager.getInstance()
    private val original = manager.globalScheme

    @AfterTest
    fun restoreScheme() = runInEdtAndWait { manager.setGlobalScheme(original) }

    @Test
    fun `the catalog keeps yesterday's scheme until it is told to reload`() {
        val settings = TailwindRainbowSettings()
        val before = settings.themeNamed(EditorSchemeThemes.NAME)

        switchScheme()

        assertEquals(before, settings.themeNamed(EditorSchemeThemes.NAME), "the catalog resolves its sources once")

        settings.reloadThemes()

        assertNotEquals(before, settings.themeNamed(EditorSchemeThemes.NAME), "reloading did not reach the theme")
    }

    @Test
    fun `the listener is what does the telling`() {
        val settings = TailwindRainbowSettings()
        val before = settings.themeNamed(EditorSchemeThemes.NAME)

        switchScheme()
        runInEdtAndWait { EditorSchemeListener().globalSchemeChange(manager.globalScheme) }

        assertNotEquals(before, currentSchemeTheme(), "the listener left the palette on the old scheme")
    }

    private fun currentSchemeTheme(): RainbowTheme = EditorSchemeThemes.themeOf(manager.globalScheme)

    private fun switchScheme() {
        val other =
            manager.allSchemes.first { it.defaultBackground.rgb != original.defaultBackground.rgb }

        runInEdtAndWait { manager.setGlobalScheme(other) }
    }
}
