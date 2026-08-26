package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ThemeEditorModelTest {
    private val builtIn =
        RainbowTheme(
            prefix =
                linkedMapOf(
                    "hover" to TextStyle("#111111", FontWeight.BOLD),
                    "focus" to TextStyle("#222222", FontWeight.BOLD),
                ),
            base = linkedMapOf("bg-*" to TextStyle("#333333", FontWeight.NORMAL)),
            arbitrary = TextStyle("#444444", FontWeight.BOLD),
            important = TextStyle("#555555", FontWeight.BOLD),
        )

    @Test
    fun `every token of the inherited theme is offered as a row`() {
        val rows = model().rows()

        assertEquals(
            listOf("hover", "focus", "bg-*", "", ""),
            rows.map { it.key },
        )
        assertEquals(
            listOf(
                SegmentKind.PREFIX,
                SegmentKind.PREFIX,
                SegmentKind.BASE,
                SegmentKind.ARBITRARY,
                SegmentKind.IMPORTANT,
            ),
            rows.map { it.section },
        )
    }

    @Test
    fun `an untouched row shows the inherited colour and is not marked overridden`() {
        val row = model().rows().first { it.key == "hover" }

        assertEquals("#111111", row.color)
        assertFalse(row.overridden)
    }

    @Test
    fun `recolouring a row changes what it shows and marks it overridden`() {
        val updated = model().recolour(SegmentKind.PREFIX, "hover", "#abcdef")
        val row = updated.rows().first { it.key == "hover" }

        assertEquals("#abcdef", row.color)
        assertTrue(row.overridden)
    }

    @Test
    fun `recolouring produces a spec holding only the changed entry`() {
        val spec = model().recolour(SegmentKind.PREFIX, "hover", "#abcdef").spec("mine")

        assertEquals("mine", spec.name)
        assertEquals(
            listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700)),
            spec.entries,
        )
    }

    @Test
    fun `resetting a row restores the inherited colour and drops the override`() {
        val reset =
            model()
                .recolour(SegmentKind.PREFIX, "hover", "#abcdef")
                .reset(SegmentKind.PREFIX, "hover")

        assertEquals("#111111", reset.rows().first { it.key == "hover" }.color)
        assertFalse(reset.rows().first { it.key == "hover" }.overridden)
        assertTrue(reset.spec("mine").entries.isEmpty())
    }

    @Test
    fun `bold is editable and stored as a font weight`() {
        val bolded = model().setBold(SegmentKind.BASE, "bg-*", bold = true)

        assertTrue(bolded.rows().first { it.key == "bg-*" }.bold)
        assertEquals(700, bolded.spec("mine").entries.single().fontWeight)

        val plain = bolded.setBold(SegmentKind.BASE, "bg-*", bold = false)
        assertEquals(400, plain.spec("mine").entries.single().fontWeight)
    }

    @Test
    fun `existing overrides are shown when the editor opens`() {
        val withOverride = model(ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "focus", "#0f0f0f", 400))))
        val row = withOverride.rows().first { it.key == "focus" }

        assertEquals("#0f0f0f", row.color)
        assertFalse(row.bold)
        assertTrue(row.overridden)
    }

    @Test
    fun `an override for a token the inherited theme lacks is still editable`() {
        val stale = model(ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "dark", "#0a0a0a", 700))))

        assertTrue(stale.rows().any { it.key == "dark" && it.overridden })
    }

    @Test
    fun `arbitrary and important carry a readable label instead of an empty key`() {
        val labels = model().rows().map { it.label }

        assertTrue("[arbitrary]" in labels)
        assertTrue("!important" in labels)
    }

    private fun model(overrides: ThemeSpec? = null) = ThemeEditorModel(builtIn, overrides)
}
