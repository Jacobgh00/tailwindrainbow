package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.TailwindDocumentScanner
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TailwindDocumentScannerTest {
    private val hover = TextStyle("#00ff00", FontWeight.BOLD)
    private val responsive = TextStyle("#ff00ff", FontWeight.BOLD)
    private val scanner = TailwindDocumentScanner()
    private val theme = RainbowTheme(prefix = mapOf("hover" to hover, "lg" to responsive))

    @Test
    fun `finds classes in an html class attribute`() {
        val source = "<div class=\"hover:bg-blue-500 lg:text-xl\"></div>"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "html").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds strings inside a Svelte class expression`() {
        val source = "<div class={active ? 'hover:bg-blue-500' : 'lg:text-xl'}></div>"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "svelte").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds nested strings passed to class helper functions`() {
        val source = "const value = clsx('hover:bg-blue-500', active && 'lg:text-xl')"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `does not colour an unrelated string that merely looks like Tailwind`() {
        val source = "const documentation = 'hover:bg-blue-500'"

        assertTrue(scan(source, "ts").isEmpty())
    }

    @Test
    fun `does not match class as a suffix of another attribute name`() {
        val source = "<div data-class=\"hover:bg-blue-500\"></div>"

        assertTrue(scan(source, "html").isEmpty())
    }

    @Test
    fun `finds strings assigned to class object properties`() {
        val source = "const options = { classes: 'hover:bg-blue-500' }"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds class attributes inside an html template string`() {
        val source = "const template = `<div class=\"hover:bg-blue-500\"></div>`"

        assertEquals(
            listOf("hover:bg-blue-500"),
            scan(source, "ts").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `finds classes in apply directives but skips comments`() {
        val source = ".button { @apply hover:bg-blue-500 lg:text-xl; } /* @apply hover:hidden; */"

        assertEquals(
            listOf("hover:bg-blue-500", "lg:text-xl"),
            scan(source, "css").map { it.sliceOf(source) },
        )
    }

    @Test
    fun `skips files larger than the configured limit`() {
        val settings = ScanSettings(maxFileSize = 10)

        assertTrue(scanner.scan("<div class=\"hover:block\"></div>", "html", settings, theme).isEmpty())
    }

    @Test
    fun `empty class identifiers disable attribute detection`() {
        val settings = ScanSettings(
            classIdentifiers = emptySet(),
            classFunctions = emptySet(),
            templateTags = emptySet(),
        )

        assertTrue(scanner.scan("<div title=\"hover:block\"></div>", "html", settings, theme).isEmpty())
    }

    private fun scan(source: String, extension: String): List<HighlightSegment> =
        scanner.scan(source, extension, ScanSettings(), theme)
}

private fun HighlightSegment.sliceOf(source: String): String = source.substring(start, end)

