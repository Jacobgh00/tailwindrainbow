package dev.tailwindrainbow.intellij.adapter.intellij.settings.ui

import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.runInEdtAndGet
import dev.tailwindrainbow.intellij.application.settings.RecognitionForm
import dev.tailwindrainbow.intellij.application.settings.SettingsForm
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals

@TestApplication
class SettingsPanelTest {
    private val rules =
        RecognitionForm(
            maxFileSize = "4096",
            classIdentifiers = "class, className",
            classFunctions = "cn, clsx",
            templateTags = "tw",
            ignoredPrefixModifiers = "group, peer",
            supportedExtensions = "html, vue",
            readsClassLikeStrings = true,
        )

    @Test
    fun `the screen hands back the rules it was shown`() {
        val shown = form(recognition = rules)

        val read = runInEdtAndGet { panelShowing(shown).read() }

        assertEquals(rules, read.recognition)
        assertEquals(shown.enabled, read.enabled)
        assertEquals(shown.themeName, read.themeName)
    }

    @Test
    fun `rules a project keeps for itself survive the screen too`() {
        val ownRules = rules.copy(supportedExtensions = "templ")
        val shown = form(recognition = rules, projectRecognition = ownRules)

        val read = runInEdtAndGet { panelShowing(shown).read() }

        assertEquals(ownRules, read.projectRecognition, "the project's rules are the ones on screen")
        assertEquals(rules, read.recognition, "the IDE's rules are kept aside, not overwritten")
    }

    @Test
    fun `themes the screen was given come back with their entries and their base`() {
        val mine = ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700)), "default")
        val shown = form(recognition = rules).copy(themeName = "mine", themes = listOf(mine))

        val read = runInEdtAndGet { panelShowing(shown).read() }

        assertEquals(listOf(mine), read.themes)
        assertEquals("mine", read.themeName)
    }

    private fun form(
        recognition: RecognitionForm,
        projectRecognition: RecognitionForm? = null,
    ) = SettingsForm(
        enabled = true,
        themeName = "default",
        recognition = recognition,
        projectRecognition = projectRecognition,
    )

    private fun panelShowing(form: SettingsForm) =
        SettingsPanel(
            baseNames = listOf("default"),
            themeNames = listOf("default", "mine"),
            basePalette = { RainbowTheme() },
            declaredVariants = { emptySet() },
        ).apply { write(form) }
}
