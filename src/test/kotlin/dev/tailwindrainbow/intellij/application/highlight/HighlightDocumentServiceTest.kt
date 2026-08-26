package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.application.port.SettingsProvider
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HighlightDocumentServiceTest {
    private val html = """<div class="hover:bg-blue-500">x</div>"""

    @Test
    fun `disabled means nothing is painted, whatever the document holds`() {
        val service = serviceWith(settings(enabled = false))

        assertTrue(service.highlight(html, "html").isEmpty())
    }

    @Test
    fun `enabled paints the configured theme's colour`() {
        val service = serviceWith(settings(themeName = "mine"))

        val segment = service.highlight(html, "html").single()

        assertEquals("#abcdef", segment.style.color)
    }

    @Test
    fun `the requested theme name is the one asked of the catalog`() {
        var asked: String? = null
        val service =
            HighlightDocumentService(
                settings = SettingsProvider { settings(themeName = "synthwave") },
                themes = { name ->
                    asked = name
                    theme
                },
            )

        service.highlight(html, "html")

        assertEquals("synthwave", asked)
    }

    @Test
    fun `an unsupported extension yields nothing`() {
        assertTrue(serviceWith(settings()).highlight(html, "kt").isEmpty())
    }

    @Test
    fun `a document past the size limit is skipped rather than scanned`() {
        val service = serviceWith(settings(scan = ScanSettings(maxFileSize = 10)))

        assertTrue(service.highlight(html, "html").isEmpty())
    }

    private fun serviceWith(current: HighlightSettings) = HighlightDocumentService({ current }, { theme })

    private fun settings(
        enabled: Boolean = true,
        themeName: String = "default",
        scan: ScanSettings = ScanSettings(),
    ) = HighlightSettings(enabled, themeName, scan)

    private val theme = RainbowTheme(prefix = mapOf("hover" to TextStyle("#abcdef", FontWeight.BOLD)))
}
