package dev.tailwindrainbow.intellij.adapter.intellij.actions

import dev.tailwindrainbow.intellij.application.highlight.HighlightingSnapshot
import dev.tailwindrainbow.intellij.application.highlight.ScanSettings
import dev.tailwindrainbow.intellij.application.port.HighlightSettings
import dev.tailwindrainbow.intellij.domain.highlight.HighlightSegment
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExplanationSnapshotTest {
    private val source = "hover:bg-blue-500"
    private val hover = HighlightSegment(0, 5, "hover", TextStyle("#ffffff", FontWeight.BOLD), SegmentKind.PREFIX)
    private val highlighting = highlighting()

    @Test
    fun `a snapshot explains its captured document at its captured caret`() {
        val snapshot =
            ExplanationSnapshot(
                source,
                "html",
                caretOffset = 2,
                documentStamp = 42,
                highlighting = highlighting,
            )

        val explained =
            snapshot.explainedBy { text, extension ->
                assertEquals(source, text)
                assertEquals("html", extension)
                listOf(hover)
            }

        assertEquals(hover, explained)
    }

    @Test
    fun `a snapshot is stale after its document caret or highlighting changes`() {
        val snapshot =
            ExplanationSnapshot(
                source,
                "html",
                caretOffset = 2,
                documentStamp = 42,
                highlighting = highlighting,
            )

        assertTrue(snapshot.isCurrent(documentStamp = 42, caretOffset = 2, highlighting = highlighting))
        assertFalse(snapshot.isCurrent(documentStamp = 43, caretOffset = 2, highlighting = highlighting))
        assertFalse(snapshot.isCurrent(documentStamp = 42, caretOffset = 3, highlighting = highlighting))
        assertFalse(snapshot.isCurrent(documentStamp = 42, caretOffset = 2, highlighting = highlighting("synthwave")))
        assertFalse(
            snapshot.isCurrent(
                documentStamp = 42,
                caretOffset = 2,
                highlighting = highlighting().copy(theme = RainbowTheme()),
            ),
        )
        val changedRecognition =
            highlighting().copy(
                settings = highlighting.settings.copy(scan = ScanSettings(readsClassLikeStrings = false)),
            )

        assertFalse(
            snapshot.isCurrent(
                documentStamp = 42,
                caretOffset = 2,
                highlighting = changedRecognition,
            ),
        )
    }

    private fun highlighting(themeName: String = "default") =
        HighlightingSnapshot(
            settings = HighlightSettings(enabled = true, themeName = themeName, scan = ScanSettings()),
            theme = RainbowTheme(prefix = mapOf("hover" to TextStyle("#ffffff", FontWeight.BOLD))),
        )
}
