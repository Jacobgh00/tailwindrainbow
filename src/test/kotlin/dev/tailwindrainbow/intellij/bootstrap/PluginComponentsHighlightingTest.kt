package dev.tailwindrainbow.intellij.bootstrap

import com.intellij.testFramework.junit5.TestApplication
import dev.tailwindrainbow.intellij.application.highlight.HighlightingSnapshot
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals

@TestApplication
class PluginComponentsHighlightingTest {
    @Test
    fun `a snapshot highlighter uses its captured theme rather than resolving it again`() {
        val snapshot =
            HighlightingSnapshot(
                settings = HighlightSettings(enabled = true, themeName = "theme-at-invocation", scan = ScanSettings()),
                theme = RainbowTheme(prefix = mapOf("hover" to TextStyle("#abcdef", FontWeight.BOLD))),
            )

        val segment =
            PluginComponents
                .highlightDocument(snapshot)
                .highlight("""<div class="hover:bg-blue-500"></div>""", "html")
                .single()

        assertEquals("#abcdef", segment.style.color)
    }
}
