package dev.tailwindrainbow.intellij.adapter.intellij.highlighting

import com.intellij.openapi.editor.colors.EditorColorsManager
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowProjectSettings
import dev.tailwindrainbow.intellij.adapter.intellij.settings.TailwindRainbowSettings
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.domain.theme.readableOn
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnnotatorTest : PaintedFileTest() {
    private val settings = TailwindRainbowSettings.getInstance()
    private val stored = settings.current()

    @AfterTest
    fun restoreSettings() {
        settings.update(stored)
        TailwindRainbowProjectSettings.getInstance(project.get()).update(null)
    }

    @Test
    fun `a supported file is painted where the scanner said it would be`() {
        val source = """<div class="hover:bg-blue-500 lg:text-xl"></div>"""

        val painted = painted("page.html", source)

        assertEquals(listOf("hover:bg-blue-500", "lg:text-xl"), painted.map { it.text })
        assertEquals(listOf(12 to 29, 30 to 40), painted.map { it.start to it.end })
    }

    @Test
    fun `a file of a type nobody asked for is left alone`() {
        val painted = painted("Notes.kt", """val markup = "<div class=\"hover:bg-blue-500\"></div>"""")

        assertTrue(painted.isEmpty(), "painted: $painted")
    }

    @Test
    fun `a file with no extension at all is skipped rather than failing`() {
        assertTrue(painted("Dockerfile", """LABEL classes="hover:bg-blue-500"""").isEmpty())
    }

    @Test
    fun `the colour painted is the one the theme's colour becomes on this background`() {
        val background = EditorColorsManager.getInstance().globalScheme.defaultBackground
        val hover = settings.themes.themeNamed("default").prefix.getValue("hover")
        val expected = hover.readableOn("#%02x%02x%02x".format(background.red, background.green, background.blue))

        val painted = painted("adapted.html", """<div class="hover:bg-blue-500"></div>""")

        assertEquals(expected.color, painted.single().color)
    }

    @Test
    fun `when the project keeps its own recognition rules, those are the ones applied`() {
        TailwindRainbowProjectSettings.getInstance(project.get())
            .update(ScanSettings(classFunctions = setOf("twcx"), supportedExtensions = setOf("ts")))

        val source = "const a = twcx('hover:bg-blue-500')\nconst b = clsx('lg:text-xl')"

        val painted = painted("rules.ts", source)

        assertEquals(listOf("hover:bg-blue-500"), painted.map { it.text }, "the project's helper is read")
    }

    @Test
    fun `switching the plugin off paints nothing at all`() {
        settings.update(HighlightSettings(enabled = false, themeName = stored.themeName, scan = stored.scan))

        assertTrue(painted("off.html", """<div class="hover:bg-blue-500"></div>""").isEmpty())
    }
}
