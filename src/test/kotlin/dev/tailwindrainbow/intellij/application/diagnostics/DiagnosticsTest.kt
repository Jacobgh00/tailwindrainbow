package dev.tailwindrainbow.intellij.application.diagnostics

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.ScanStatus
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

class DiagnosticsTest {
    @Test
    fun `the report names the plugin version, the IDE it runs in, and the theme in use`() {
        val report = diagnostics().report()

        assertContains(report, "0.1.0")
        assertContains(report, "IntelliJ IDEA 2025.2 (IC-252.23892.409)")
        assertContains(report, "midnight")
    }

    @Test
    fun `a file that was not scanned says why, rather than saying nothing`() {
        val report = diagnostics(file = ScannedFile("html", ScanStatus.TOO_LARGE)).report()

        assertContains(report, "html")
        assertTrue("size" in report, "the reason a large file is skipped has to be readable, got:\n$report")
    }

    @Test
    fun `an unsupported extension is reported as such`() {
        val report = diagnostics(file = ScannedFile("kt", ScanStatus.NOT_SUPPORTED)).report()

        assertTrue("supported" in report, "got:\n$report")
    }

    @Test
    fun `colouring switched off is visible without reading the settings`() {
        val report = diagnostics(settings = settings(enabled = false)).report()

        assertContains(report, "off")
    }

    @Test
    fun `a report with no file open is still a report`() {
        val report = diagnostics(file = null).report()

        assertContains(report, "0.1.0")
        assertTrue("no file" in report, "got:\n$report")
    }

    @Test
    fun `the recognition rules are listed, and say whether the project or the IDE owns them`() {
        val report = diagnostics(recognitionFromProject = true).report()

        assertContains(report, "twMerge")
        assertContains(report, "project")
    }

    @Test
    fun `recognition rules that come from the IDE do not claim to come from the project`() {
        val report = diagnostics(recognitionFromProject = false).report()

        assertTrue("project" !in report, "got:\n$report")
    }

    @Test
    fun `a dropped theme entry is reported with the reason the parser gave`() {
        val report = diagnostics(themeProblems = listOf(problem)).report()

        assertContains(report, "midnight: prefix 'hover' — colour is wrong")
    }

    @Test
    fun `themes with nothing wrong say so, so an empty section is not mistaken for a truncated paste`() {
        val report = diagnostics().report()

        assertContains(report, "none")
    }

    private val problem = ThemeProblem("midnight", SegmentKind.PREFIX, "hover", "colour is wrong")

    private fun settings(
        enabled: Boolean = true,
        themeName: String = "midnight",
    ) = HighlightSettings(enabled, themeName, ScanSettings())

    private fun diagnostics(
        settings: HighlightSettings = settings(),
        recognitionFromProject: Boolean = false,
        file: ScannedFile? = ScannedFile("html", ScanStatus.SCANNED),
        themeProblems: List<ThemeProblem> = emptyList(),
    ) = Diagnostics(
        pluginVersion = "0.1.0",
        ide = "IntelliJ IDEA 2025.2 (IC-252.23892.409)",
        settings = settings,
        recognitionFromProject = recognitionFromProject,
        file = file,
        themeProblems = themeProblems,
    )
}
