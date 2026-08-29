package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.application.port.Cancellation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CancellationTest {
    private val scanner = TailwindDocumentScanner()
    private val source = (1..50).joinToString("\n") { """<div class="hover:bg-blue-500 lg:text-xl"></div>""" }

    @Test
    fun `a scan gives up part way when the caller has stopped wanting it`() {
        var checks = 0

        assertFailsWith<GaveUp> {
            scanner.scan(source, "html", ScanSettings(), BuiltInThemes.default) {
                if (++checks > 3) throw GaveUp()
            }
        }

        assertTrue(checks < 50, "it stopped rather than finishing the file: $checks checks")
    }

    @Test
    fun `nobody asking it to stop means it runs to the end`() {
        val painted = scanner.scan(source, "html", ScanSettings(), BuiltInThemes.default, Cancellation.NONE)

        assertEquals(100, painted.size)
    }

    @Test
    fun `looking for uncoloured variants gives up part way too`() {
        var checks = 0

        assertFailsWith<GaveUp> {
            UncolouredVariants(
                settings = ScanSettings(),
                theme = BuiltInThemes.default,
                declared = setOf("theme-midnight"),
            ) { if (++checks > 3) throw GaveUp() }.inside(source, "html")
        }

        assertTrue(checks < 50, "it stopped rather than finishing the file: $checks checks")
    }

    private class GaveUp : RuntimeException()
}
