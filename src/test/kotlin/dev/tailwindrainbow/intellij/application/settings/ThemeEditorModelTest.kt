package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
    fun `an untouched row shows the inherited colour and is only inherited`() {
        val row = model().rows().first { it.key == "hover" }

        assertEquals("#111111", row.style.color)
        assertEquals(RowOrigin.INHERITED, row.origin)
        assertFalse(row.isUserDefined)
    }

    @Test
    fun `recolouring a row changes what it shows and marks it overridden`() {
        val updated = model().restyle(SegmentKind.PREFIX, "hover", RowStyle("#abcdef"))
        val row = updated.rows().first { it.key == "hover" }

        assertEquals("#abcdef", row.style.color)
        assertEquals(RowOrigin.OVERRIDDEN, row.origin)
    }

    @Test
    fun `recolouring produces a spec holding only the changed entry`() {
        val spec = model().restyle(SegmentKind.PREFIX, "hover", RowStyle("#abcdef")).spec("mine")

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
                .restyle(SegmentKind.PREFIX, "hover", RowStyle("#abcdef"))
                .reset(SegmentKind.PREFIX, "hover")

        assertEquals("#111111", reset.rows().first { it.key == "hover" }.style.color)
        assertEquals(RowOrigin.INHERITED, reset.rows().first { it.key == "hover" }.origin)
        assertTrue(reset.spec("mine").entries.isEmpty())
    }

    @Test
    fun `bold is editable and stored as a font weight`() {
        val bolded = model().restyle(SegmentKind.BASE, "bg-*", RowStyle("#333333", bold = true))

        assertTrue(bolded.rows().first { it.key == "bg-*" }.style.bold)
        assertEquals(700, bolded.spec("mine").entries.single().fontWeight)

        val plain = bolded.restyle(SegmentKind.BASE, "bg-*", RowStyle("#333333", bold = false))
        assertEquals(400, plain.spec("mine").entries.single().fontWeight)
    }

    @Test
    fun `a row can be switched off and keeps the colour it had`() {
        val off = model().restyle(SegmentKind.PREFIX, "hover", RowStyle("#111111", enabled = false))
        val row = off.rows().first { it.key == "hover" }

        assertFalse(row.style.enabled)
        assertEquals("#111111", row.style.color)
        assertEquals(
            listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#111111", 700, enabled = false)),
            off.spec("mine").entries,
        )
    }

    @Test
    fun `an entry stored as switched off opens that way`() {
        val stored =
            model(
                ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#111111", 700, enabled = false))),
            )

        assertFalse(stored.rows().first { it.key == "hover" }.style.enabled)
    }

    @Test
    fun `existing overrides are shown when the editor opens`() {
        val withOverride = model(ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "focus", "#0f0f0f", 400))))
        val row = withOverride.rows().first { it.key == "focus" }

        assertEquals("#0f0f0f", row.style.color)
        assertFalse(row.style.bold)
        assertEquals(RowOrigin.OVERRIDDEN, row.origin)
    }

    @Test
    fun `an override for a token the inherited theme lacks counts as added`() {
        val stale = model(ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "dark", "#0a0a0a", 700))))

        assertEquals(RowOrigin.ADDED, stale.rows().first { it.key == "dark" }.origin)
    }

    @Test
    fun `a malformed stored entry still appears as a row, so it can be repaired`() {
        val broken =
            model(ThemeSpec("mine", listOf(StyleEntry(SegmentKind.PREFIX, "hover", "not-a-colour", 700))))

        val row = broken.rows().first { it.key == "hover" }

        assertEquals("not-a-colour", row.style.color)
        assertEquals(RowOrigin.OVERRIDDEN, row.origin, "hiding it would leave the user nothing to reset")
    }

    @Test
    fun `arbitrary and important carry a readable label instead of an empty key`() {
        val labels = model().rows().map { it.label }

        assertTrue("[arbitrary]" in labels)
        assertTrue("!important" in labels)
    }

    @Test
    fun `a token the inherited theme lacks can be added and then coloured`() {
        val added =
            model()
                .add(SegmentKind.PREFIX, "focus-visible")
                .restyle(SegmentKind.PREFIX, "focus-visible", RowStyle("#abcdef"))

        val row = added.rows().first { it.key == "focus-visible" }
        assertEquals(RowOrigin.ADDED, row.origin)
        assertEquals("#abcdef", row.style.color)
        assertEquals(
            listOf(StyleEntry(SegmentKind.PREFIX, "focus-visible", "#abcdef", 700)),
            added.spec("mine").entries,
        )
    }

    @Test
    fun `an added token can be removed again`() {
        val removed =
            model()
                .add(SegmentKind.BASE, "text-*")
                .remove(SegmentKind.BASE, "text-*")

        assertTrue(removed.rows().none { it.key == "text-*" })
        assertTrue(removed.spec("mine").entries.isEmpty())
    }

    @Test
    fun `the same token can be added to a second section`() {
        val added = model().add(SegmentKind.BASE, "hover")

        assertEquals(
            listOf(SegmentKind.PREFIX, SegmentKind.BASE),
            added.rows().filter { it.key == "hover" }.map { it.section },
        )
    }

    @Test
    fun `added tokens are listed inside their own section`() {
        val rows =
            model()
                .add(SegmentKind.BASE, "text-*")
                .add(SegmentKind.PREFIX, "focus-visible")
                .rows()

        assertEquals(
            listOf("hover", "focus", "focus-visible", "bg-*", "text-*", "", ""),
            rows.map { it.key },
        )
        assertEquals(
            listOf(
                SegmentKind.PREFIX,
                SegmentKind.PREFIX,
                SegmentKind.PREFIX,
                SegmentKind.BASE,
                SegmentKind.BASE,
                SegmentKind.ARBITRARY,
                SegmentKind.IMPORTANT,
            ),
            rows.map { it.section },
        )
    }

    @Test
    fun `a token already in the editor cannot be added twice`() {
        assertTrue(model().holds(SegmentKind.PREFIX, "hover"))
        assertFalse(model().holds(SegmentKind.BASE, "hover"))

        assertFailsWith<IllegalArgumentException> { model().add(SegmentKind.PREFIX, "hover") }
        assertFailsWith<IllegalArgumentException> {
            model().add(SegmentKind.PREFIX, "focus-visible").add(SegmentKind.PREFIX, "focus-visible")
        }
    }

    @Test
    fun `a section holding a single style takes no added token`() {
        assertFailsWith<IllegalArgumentException> { model().add(SegmentKind.ARBITRARY, "whatever") }
    }

    @Test
    fun `a blank token cannot be added`() {
        assertFailsWith<IllegalArgumentException> { model().add(SegmentKind.PREFIX, "  ") }
    }

    @Test
    fun `an inherited token is reset rather than removed`() {
        val recoloured = model().restyle(SegmentKind.PREFIX, "hover", RowStyle("#abcdef"))

        assertFailsWith<IllegalArgumentException> { recoloured.remove(SegmentKind.PREFIX, "hover") }
    }

    @Test
    fun `an added token is removed rather than reset`() {
        val added = model().add(SegmentKind.PREFIX, "focus-visible")

        assertFailsWith<IllegalArgumentException> { added.reset(SegmentKind.PREFIX, "focus-visible") }
    }

    @Test
    fun `a token added in the editor becomes part of the resolved theme`() {
        val spec =
            model()
                .add(SegmentKind.PREFIX, "focus-visible")
                .restyle(SegmentKind.PREFIX, "focus-visible", RowStyle("#abcdef"))
                .spec("mine")

        val resolved = ThemeRepository(ThemeSource { mapOf("mine" to builtIn) }, SpecThemeSource(listOf(spec)))

        assertEquals(TextStyle("#abcdef", FontWeight.BOLD), resolved.find("mine").prefix["focus-visible"])
        assertEquals(TextStyle("#111111", FontWeight.BOLD), resolved.find("mine").prefix["hover"])
    }

    private fun model(overrides: ThemeSpec? = null) = ThemeEditorModel(builtIn, overrides)
}
