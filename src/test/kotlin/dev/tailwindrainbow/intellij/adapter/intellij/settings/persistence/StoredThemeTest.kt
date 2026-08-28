package dev.tailwindrainbow.intellij.adapter.intellij.settings.persistence

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals

class StoredThemeTest {
    @Test
    fun `a theme written before bases existed is based on itself`() {
        val stored = StoredTheme().apply { name = "default" }

        assertEquals("default", stored.toSpec().basedOn)
    }

    @Test
    fun `a spec survives the round trip through storage`() {
        val spec =
            ThemeSpec(
                name = "midnight",
                entries = listOf(StyleEntry(SegmentKind.PREFIX, "hover", "#abcdef", 700, enabled = false)),
                basedOn = "synthwave",
            )

        assertEquals(spec, StoredTheme.of(spec).toSpec())
    }
}
