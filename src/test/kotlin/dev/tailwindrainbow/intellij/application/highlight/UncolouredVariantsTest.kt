package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UncolouredVariantsTest {
    private val theme = RainbowTheme(prefix = mapOf("hover" to TextStyle("#111111", FontWeight.BOLD)))
    private val source = """<div class="pointer-coarse:p-4 hover:underline"></div>"""

    @Test
    fun `a variant the project declares and the theme does not colour is reported, where it is written`() {
        val found = UncolouredVariants(ScanSettings(), theme, setOf("pointer-coarse")).inside(source, "html")

        assertEquals(listOf("pointer-coarse"), found.map { it.name })
        assertEquals("pointer-coarse", source.substring(found.single().start, found.single().end))
    }

    @Test
    fun `a variant the theme already colours is nobody's problem`() {
        assertTrue(UncolouredVariants(ScanSettings(), theme, setOf("hover")).inside(source, "html").isEmpty())
    }

    @Test
    fun `a project that declares nothing reports nothing`() {
        assertTrue(UncolouredVariants(ScanSettings(), theme, emptySet()).inside(source, "html").isEmpty())
    }

    @Test
    fun `a declared variant nobody wrote in this file is not reported`() {
        val found = UncolouredVariants(ScanSettings(), theme, setOf("theme-midnight")).inside(source, "html")

        assertTrue(found.isEmpty(), "the file has to mention it")
    }

    @Test
    fun `every place the variant is written is reported`() {
        val twice = """<div class="pointer-coarse:p-4"><p class="pointer-coarse:text-sm"></p></div>"""

        val found = UncolouredVariants(ScanSettings(), theme, setOf("pointer-coarse")).inside(twice, "html")

        assertEquals(2, found.size)
    }

    @Test
    fun `a variant behind a scoping modifier is reported on the underlying variant`() {
        val source = """<div class="group-custom:bg-blue-500"></div>"""

        val found = UncolouredVariants(ScanSettings(), theme, setOf("custom")).inside(source, "html")

        assertEquals(listOf("custom"), found.map { it.name })
        assertEquals("custom", source.substring(found.single().start, found.single().end))
    }

    @Test
    fun `a stacked scoped variant split by an important marker is reported once`() {
        val source = """<div class="peer-group-custom:!bg-blue-500"></div>"""

        val found = UncolouredVariants(ScanSettings(), theme, setOf("custom")).inside(source, "html")

        assertEquals(listOf("custom"), found.map { it.name })
        assertEquals("custom", source.substring(found.single().start, found.single().end))
    }
}
