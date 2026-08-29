package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VsCodeSettingsTest {
    private val settingsJson =
        """
        {
          "editor.fontSize": 14,
          "tailwindRainbow.theme": "myTheme",
          "tailwindRainbow.themes": {
            "myTheme": {
              "prefix": {
                "hover": { "color": "#ff0000", "fontWeight": "bold" },
                "focus": { "color": "#00ff00", "fontWeight": "normal" }
              },
              "base": { "bg-*": { "color": "#ff6600" } },
              "arbitrary": { "color": "#ff00ff" },
              "important": { "color": "#ff0000", "fontWeight": "bold" }
            },
            "quiet": {
              "prefix": { "hover": { "color": "#334455" } }
            }
          },
          "workbench.colorTheme": "Default Dark+"
        }
        """.trimIndent()

    @Test
    fun `every theme in a VS Code settings file comes across`() {
        val themes = themesFromFile(settingsJson)

        assertEquals(listOf("myTheme", "quiet"), themes.map { it.name })
    }

    @Test
    fun `the settings around the themes are ignored rather than read as themes`() {
        val themes = themesFromFile(settingsJson)

        assertTrue(themes.none { it.name.startsWith("editor.") || it.name.startsWith("workbench.") })
    }

    @Test
    fun `a theme keeps its sections, colours, and weights`() {
        val mine = themesFromFile(settingsJson).first { it.name == "myTheme" }

        assertEquals(
            listOf(
                StyleEntry(SegmentKind.PREFIX, "hover", "#ff0000", 700),
                StyleEntry(SegmentKind.PREFIX, "focus", "#00ff00", 400),
                StyleEntry(SegmentKind.BASE, "bg-*", "#ff6600", 400),
                StyleEntry(SegmentKind.ARBITRARY, "", "#ff00ff", 400),
                StyleEntry(SegmentKind.IMPORTANT, "", "#ff0000", 700),
            ),
            mine.entries,
        )
    }

    @Test
    fun `a settings file with comments in it still reads`() {
        val commented =
            """
            {
              // the theme I use
              "tailwindRainbow.themes": { "mine": { "prefix": { "hover": { "color": "#ff0000" } } } }
            }
            """.trimIndent()

        assertEquals(listOf("mine"), themesFromFile(commented).map { it.name })
    }

    @Test
    fun `a settings file with no themes in it brings nothing across`() {
        assertTrue(themesFromFile("""{ "editor.fontSize": 14 }""").none { it.entries.isNotEmpty() })
    }
}
