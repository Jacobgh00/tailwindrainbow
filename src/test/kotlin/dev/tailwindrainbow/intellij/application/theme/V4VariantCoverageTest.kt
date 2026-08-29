package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.highlight.TailwindDocumentScanner
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import kotlin.test.Test
import kotlin.test.assertTrue

class V4VariantCoverageTest {
    private val scanner = TailwindDocumentScanner()

    private val v4Classes =
        listOf(
            "@md:p-4",
            "@max-lg:flex",
            "@[400px]:block",
            "@container/main:block",
            "data-[state=open]:bg-red-500",
            "data-open:bg-red-500",
            "aria-expanded:rotate-180",
            "supports-[display:grid]:grid",
            "nth-3:underline",
            "nth-of-type-3:p-4",
            "starting:opacity-0",
            "open:p-4",
            "inert:opacity-50",
            "has-[:checked]:bg-blue-50",
            "group-[.is-open]/menu:flex",
        )

    @Test
    fun `every built-in theme colours the variants v4 introduced`() {
        BuiltInThemes.themes().forEach { (name, theme) ->
            v4Classes.forEach { classes ->
                assertTrue(painted(classes, theme).isNotEmpty(), "$name leaves $classes uncoloured")
            }
        }
    }

    @Test
    fun `the two themes colour them differently, as they do everything else`() {
        val differing =
            v4Classes.count { classes ->
                painted(classes, BuiltInThemes.default) != painted(classes, BuiltInThemes.synthwave)
            }

        assertTrue(differing > v4Classes.size / 2, "only $differing of ${v4Classes.size} differ")
    }

    private fun painted(
        classes: String,
        theme: RainbowTheme,
    ): List<String> {
        val source = """<div class="$classes"></div>"""

        return scanner.scan(source, "html", ScanSettings(), theme).map { it.style.color }
    }
}
