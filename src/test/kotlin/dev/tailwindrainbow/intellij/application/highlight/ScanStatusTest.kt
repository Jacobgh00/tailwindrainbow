package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class ScanStatusTest {
    private val settings =
        HighlightSettings(
            enabled = true,
            themeName = "default",
            scan = ScanSettings(maxFileSize = 100, supportedExtensions = setOf("html", "vue")),
        )

    @Test
    fun `a supported file within the limit is scanned`() {
        assertEquals(ScanStatus.SCANNED, settings.statusFor("html", textLength = 50))
        assertEquals(ScanStatus.SCANNED, settings.statusFor("HTML", textLength = 50), "case is not the file's fault")
    }

    @Test
    fun `a file type nobody asked for is left alone`() {
        assertEquals(ScanStatus.NOT_SUPPORTED, settings.statusFor("kt", textLength = 50))
    }

    @Test
    fun `a file past the limit says so, rather than looking unsupported`() {
        assertEquals(ScanStatus.TOO_LARGE, settings.statusFor("html", textLength = 101))
    }

    @Test
    fun `switching the plugin off outranks everything else`() {
        assertEquals(ScanStatus.DISABLED, settings.copy(enabled = false).statusFor("html", textLength = 50))
    }
}
