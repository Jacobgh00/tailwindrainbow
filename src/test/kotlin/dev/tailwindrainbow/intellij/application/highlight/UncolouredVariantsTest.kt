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
        val found = uncolouredDeclaredVariants(source, "html", ScanSettings(), theme, setOf("pointer-coarse"))

        assertEquals(listOf("pointer-coarse"), found.map { it.name })
        assertEquals("pointer-coarse", source.substring(found.single().start, found.single().end))
    }

    @Test
    fun `a variant the theme already colours is nobody's problem`() {
        assertTrue(uncolouredDeclaredVariants(source, "html", ScanSettings(), theme, setOf("hover")).isEmpty())
    }

    @Test
    fun `a project that declares nothing reports nothing`() {
        assertTrue(uncolouredDeclaredVariants(source, "html", ScanSettings(), theme, emptySet()).isEmpty())
    }

    @Test
    fun `a declared variant nobody wrote in this file is not reported`() {
        val found = uncolouredDeclaredVariants(source, "html", ScanSettings(), theme, setOf("theme-midnight"))

        assertTrue(found.isEmpty(), "the file has to mention it")
    }

    @Test
    fun `every place the variant is written is reported`() {
        val twice = """<div class="pointer-coarse:p-4"><p class="pointer-coarse:text-sm"></p></div>"""

        val found = uncolouredDeclaredVariants(twice, "html", ScanSettings(), theme, setOf("pointer-coarse"))

        assertEquals(2, found.size)
    }
}
