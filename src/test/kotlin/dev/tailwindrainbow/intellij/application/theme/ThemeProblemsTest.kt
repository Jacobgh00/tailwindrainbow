package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThemeProblemsTest {
    @Test
    fun `themes the user can edit into shape report nothing`() {
        val fine = listOf(spec(entry(color = "#abcdef")))

        assertTrue(problemsIntroducedBy(pending = fine, stored = fine).isEmpty())
    }

    @Test
    fun `a problem the pending themes add is reported`() {
        val broken = listOf(spec(entry(color = "not-a-colour")))

        val introduced = problemsIntroducedBy(pending = broken, stored = emptyList())

        assertEquals(1, introduced.size)
        assertEquals("hover", introduced.single().key)
    }

    @Test
    fun `a problem the stored themes already have does not block the user`() {
        val broken = listOf(spec(entry(color = "not-a-colour")))

        assertTrue(problemsIntroducedBy(pending = broken, stored = broken).isEmpty())
    }

    @Test
    fun `a description names the theme, the entry, and what is wrong with it`() {
        val problem = SpecThemeSource(listOf(spec(entry(color = "not-a-colour")))).problems.single()

        val description = problem.describe()

        assertTrue(description.contains("mine"), description)
        assertTrue(description.contains("hover"), description)
        assertTrue(description.contains("#RRGGBB"), description)
    }

    private fun spec(vararg entries: StyleEntry) = ThemeSpec("mine", entries.toList())

    private fun entry(
        key: String = "hover",
        color: String = "#123456",
        fontWeight: Int = 700,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, fontWeight)
}
