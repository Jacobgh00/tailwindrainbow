package dev.tailwindrainbow.intellij.adapter.intellij.settings

import com.intellij.testFramework.LoggedErrorProcessor
import com.intellij.testFramework.junit5.TestApplication
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertTrue

@TestApplication
class DroppedEntryLoggingTest {
    @Test
    fun `an entry the parser drops is named in the log`() {
        val warnings = warningsWhile { it.update(settings, listOf(themeWith(colour = "not-a-colour"))) }

        assertTrue(
            warnings.any { "midnight" in it && "hover" in it },
            "the theme and the entry both have to be in the message, got: $warnings",
        )
    }

    @Test
    fun `a theme the parser accepts whole logs nothing`() {
        val warnings = warningsWhile { it.update(settings, listOf(themeWith(colour = "#abcdef"))) }

        assertTrue(warnings.isEmpty(), "nothing was dropped, so nothing should be reported, got: $warnings")
    }

    private val settings = HighlightSettings(enabled = true, themeName = "midnight", scan = ScanSettings())

    private fun themeWith(colour: String) = ThemeSpec("midnight", listOf(hover(colour)))

    private fun hover(colour: String) = StyleEntry(SegmentKind.PREFIX, "hover", colour, 700)

    private fun warningsWhile(change: (TailwindRainbowSettings) -> Unit): List<String> {
        val recorded = mutableListOf<String>()

        LoggedErrorProcessor.executeWith<Throwable>(recorder(recorded)) { change(TailwindRainbowSettings()) }

        return recorded
    }

    private fun recorder(into: MutableList<String>) =
        object : LoggedErrorProcessor() {
            override fun processWarn(
                category: String,
                message: String,
                t: Throwable?,
            ): Boolean {
                into += message

                return false
            }
        }
}
