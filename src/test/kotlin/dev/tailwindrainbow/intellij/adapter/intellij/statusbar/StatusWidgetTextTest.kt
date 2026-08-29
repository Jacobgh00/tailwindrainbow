package dev.tailwindrainbow.intellij.adapter.intellij.statusbar

import com.intellij.testFramework.junit5.TestApplication
import dev.tailwindrainbow.intellij.application.highlight.ScanStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestApplication
class StatusWidgetTextTest {
    @Test
    fun `a scanned file shows the theme colouring it`() {
        assertEquals("Tailwind: synthwave", widgetText(ScanStatus.SCANNED, "synthwave"))
    }

    @Test
    fun `a file that is not scanned says so, rather than naming a theme`() {
        listOf(ScanStatus.NOT_SUPPORTED, ScanStatus.TOO_LARGE).forEach {
            assertEquals("Tailwind: not scanned", widgetText(it, "synthwave"))
        }
    }

    @Test
    fun `switched off is told apart from not scanned`() {
        assertEquals("Tailwind: off", widgetText(ScanStatus.DISABLED, "synthwave"))
    }

    @Test
    fun `the tooltip says which reason applies`() {
        assertTrue(widgetTooltip(ScanStatus.NOT_SUPPORTED, "default", "kt", 1000).contains("kt"))
        assertTrue(widgetTooltip(ScanStatus.TOO_LARGE, "default", "html", 1000).contains("1,000"))
        assertTrue(widgetTooltip(ScanStatus.SCANNED, "synthwave", "html", 1000).contains("synthwave"))
    }
}
