package dev.tailwindrainbow.intellij.application.highlight

import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.isHexColor
import dev.tailwindrainbow.intellij.domain.theme.readableOn
import kotlin.test.Test
import kotlin.test.assertTrue

class ScanInvariantsTest {
    @Test
    fun `every segment names a range the document actually has`() {
        forEachHostileDocument { text, extension, segments ->
            segments.forEach { segment ->
                assertTrue(
                    segment.start in 0..segment.end && segment.end <= text.length,
                    "$extension gave ${segment.start}..${segment.end} for ${text.length} characters",
                )
            }
        }
    }

    @Test
    fun `the hostile documents are not simply refused wholesale`() {
        var painted = 0

        forEachHostileDocument { _, _, segments -> painted += segments.size }

        assertTrue(painted > HOSTILE_DOCUMENTS.size, "the corpus painted $painted segments")
    }

    @Test
    fun `every segment carries a colour the platform can decode`() {
        forEachHostileDocument { _, extension, segments ->
            segments.forEach { segment ->
                assertTrue(segment.style.color.isHexColor(), "$extension painted ${segment.style.color}")
            }
        }
    }

    @Test
    fun `a colour stays decodable after being made readable on any background`() {
        val styles = BuiltInThemes.themes().values.flatMap { it.prefix.values + it.base.values }

        BACKGROUNDS.forEach { background ->
            styles.forEach { style ->
                val readable = style.readableOn(background).color

                assertTrue(readable.isHexColor(), "${style.color} on $background became $readable")
            }
        }
    }

    private fun forEachHostileDocument(check: (String, String, List<HighlightSegment>) -> Unit) {
        val service =
            HighlightDocumentService(
                { HighlightSettings(enabled = true, themeName = BuiltInThemes.DEFAULT_NAME, scan = ScanSettings()) },
                { name -> BuiltInThemes.themes().getValue(name) },
            )

        HOSTILE_DOCUMENTS.forEach { text ->
            EXTENSIONS.forEach { extension -> check(text, extension, service.highlight(text, extension)) }
        }
    }

    private companion object {
        val EXTENSIONS = listOf("html", "css", "js", "tsx", "vue", "svelte")

        val BACKGROUNDS = listOf("#000000", "#ffffff", "#2b2b2b", "#7f7f7f", "#010203", "#fefefe")

        val HOSTILE_DOCUMENTS =
            listOf(
                "",
                " ",
                "class=",
                """<div class="hover:">""",
                """<div class="::::">""",
                """<div class="hover:bg-red-500""",
                """<div class='hover:bg-red-500 "quoted"'>""",
                """<div class="peer-[aspect-ratio:1/8]:bg-red-500">""",
                """<div class="[&>*]:">""",
                """<div class="hÖver:bg-red-500 🎨:text-black">""",
                "<div class=\"hover:bg-red-500\r\n\tfocus:bg-blue-500\">",
                "@apply hover:bg-red-500",
                "@apply",
                "const a = `class=\"hover:bg-red-500`",
                """<div class="${'$'}{cond ? 'hover:bg-red-500' : 'focus:'}">""",
                """<div class="${"hover:".repeat(2_000)}">""",
                """<div class="${"a".repeat(10_000)}:bg-red-500">""",
                " <div class=\"hover:bg-red-500\">",
            )
    }
}
