package dev.tailwindrainbow.intellij.bootstrap

import com.intellij.testFramework.LightVirtualFile
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import dev.tailwindrainbow.intellij.application.diagnostics.report
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

@TestApplication
class DiagnosticsGatheringTest {
    private val project = projectFixture()

    @Test
    fun `the report names the IDE the plugin is running in`() {
        val report = PluginComponents.diagnostics(project.get(), file = null).report()

        val ide = report.lineSequence().first { it.startsWith("IDE: ") }

        assertTrue(ide.length > "IDE: ".length, "the IDE has to identify itself, got: '$ide'")
    }

    @Test
    fun `the report names the theme the editor is painting with`() {
        val report = PluginComponents.diagnostics(project.get(), file = null).report()

        assertContains(report, "Theme: default")
    }

    @Test
    fun `an open file is reported with its extension and whether it was scanned`() {
        val html = LightVirtualFile("sample.html", """<div class="hover:underline">x</div>""")

        val report = PluginComponents.diagnostics(project.get(), html).report()

        assertContains(report, ".html — scanned")
    }

    @Test
    fun `a file the plugin does not recognise is reported as skipped, with the reason`() {
        val kotlin = LightVirtualFile("Sample.kt", "val x = 1")

        val report = PluginComponents.diagnostics(project.get(), kotlin).report()

        assertContains(report, ".kt — not scanned, the extension is not in the supported list")
    }
}
