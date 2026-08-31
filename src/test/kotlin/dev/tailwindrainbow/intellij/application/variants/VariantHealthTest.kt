package dev.tailwindrainbow.intellij.application.variants

import dev.tailwindrainbow.intellij.application.port.ThemeHealthCatalog
import dev.tailwindrainbow.intellij.application.theme.ThemeEntryProvenance
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthContext
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthEntry
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class VariantHealthTest {
    @Test
    fun `reports a declaration with no matching entry`() {
        val report = analyze(declaration("supports-grid"))

        assertIs<VariantStatus.MissingColour>(report.assessments.single().status)
        assertEquals(listOf(VariantProblem.MissingColour), report.problems)
    }

    @Test
    fun `distinguishes a disabled entry from an absent entry`() {
        val report =
            analyze(
                declaration("supports-grid"),
                theme = themeWith(entry("supports-grid", EntryStatus.DISABLED)),
            )

        assertIs<VariantStatus.Disabled>(report.assessments.single().status)
        assertIs<VariantProblem.DisabledEntry>(report.problems.single())
    }

    @Test
    fun `reports a disabled wildcard entry instead of calling it absent`() {
        val report =
            analyze(
                declaration("supports-grid"),
                theme = themeWith(entry("supports-*", EntryStatus.DISABLED)),
            )

        assertIs<VariantStatus.Disabled>(report.assessments.single().status)
        assertIs<VariantProblem.DisabledEntry>(report.problems.single())
    }

    @Test
    fun `distinguishes a malformed override from an absent entry`() {
        val malformed = entry("supports-grid", EntryStatus.INVALID, problem = "bad colour")
        val report = analyze(declaration("supports-grid"), theme = themeWith(malformed))

        assertIs<VariantStatus.Invalid>(report.assessments.single().status)
        assertEquals("bad colour", assertIs<VariantProblem.InvalidEntry>(report.problems.single()).entry.problem)
    }

    @Test
    fun `reports a malformed wildcard override instead of calling it absent`() {
        val malformed = entry("supports-*", EntryStatus.INVALID, problem = "bad colour")
        val report = analyze(declaration("supports-grid"), theme = themeWith(malformed))

        assertIs<VariantStatus.Invalid>(report.assessments.single().status)
        assertEquals("supports-*", assertIs<VariantProblem.InvalidEntry>(report.problems.single()).entry.key)
    }

    @Test
    fun `retains a malformed finding when an inherited wildcard still colours the variant`() {
        val inherited = entry("supports-*", EntryStatus.USABLE, ThemeEntryProvenance.BASE, "default")
        val malformed = entry("supports-*", EntryStatus.INVALID, problem = "bad colour")
        val report = analyze(declaration("supports-grid"), theme = themeWith(inherited, malformed))

        assertIs<VariantStatus.Coloured>(report.assessments.single().status)
        assertTrue(report.problems.any { it is VariantProblem.InvalidEntry })
    }

    @Test
    fun `reports a wildcard winner without treating coverage as a defect`() {
        val wildcard = entry("supports-*", EntryStatus.USABLE, ThemeEntryProvenance.BASE, "default")
        val report = analyze(declaration("supports-grid"), theme = themeWith(wildcard))

        val coloured = assertIs<VariantStatus.Coloured>(report.assessments.single().status)
        assertEquals(VariantMatchKind.WILDCARD, coloured.match.kind)
        assertEquals(ThemeEntryProvenance.BASE, coloured.match.entry.provenance)
        assertTrue(report.problems.isEmpty())
    }

    @Test
    fun `groups repeated declarations into one assessment and one duplicate finding`() {
        val report = analyze(declaration("supports-grid", "one.css"), declaration("supports-grid", "two.css"))

        assertEquals(1, report.assessments.size)
        assertEquals(2, report.assessments.single().declarations.size)
        assertEquals(
            listOf(VariantProblem.DuplicateDeclaration, VariantProblem.MissingColour),
            report.problems,
        )
    }

    @Test
    fun `an exact enabled entry wins over a wildcard`() {
        val exact = entry("supports-grid", EntryStatus.USABLE, ThemeEntryProvenance.USER_OVERRIDE, "mine")
        val wildcard = entry("supports-*", EntryStatus.USABLE, ThemeEntryProvenance.BASE, "default")
        val report = analyze(declaration("supports-grid"), theme = themeWith(exact, wildcard))

        val coloured = assertIs<VariantStatus.Coloured>(report.assessments.single().status)
        assertEquals("supports-grid", coloured.match.key)
        assertEquals(VariantMatchKind.EXACT, coloured.match.kind)
        assertTrue(report.problems.isEmpty())
    }

    private fun analyze(
        vararg declarations: VariantDeclaration,
        theme: ThemeHealthContext = ThemeHealthContext("default", RainbowTheme(), emptyList()),
    ): VariantHealthReport {
        return VariantHealthAnalyzer(ThemeHealthCatalog { theme }, emptySet())
            .analyze("default", VariantScanResult(declarations.toList(), 0))
    }

    private fun themeWith(vararg entries: ThemeHealthEntry): ThemeHealthContext {
        val effective =
            entries
                .mapNotNull { entry ->
                    when (entry) {
                        is ThemeHealthEntry.Usable ->
                            entry.key to TextStyle(entry.color, FontWeight.of(entry.fontWeight), enabled = true)
                        is ThemeHealthEntry.Disabled ->
                            entry.key to TextStyle(entry.color, FontWeight.of(entry.fontWeight), enabled = false)
                        is ThemeHealthEntry.Invalid -> null
                    }
                }.toMap()

        return ThemeHealthContext(
            name = "default",
            effectiveTheme = RainbowTheme(prefix = effective),
            entries = entries.toList(),
        )
    }

    private fun declaration(
        name: String,
        path: String = "tailwind.css",
    ): VariantDeclaration {
        return VariantDeclaration(
            name = name,
            kind = VariantDeclarationKind.CUSTOM_VARIANT,
            location = VariantSourceLocation(path, 0, name.length),
        )
    }

    private fun entry(
        key: String,
        status: EntryStatus,
        provenance: ThemeEntryProvenance = ThemeEntryProvenance.USER_OVERRIDE,
        sourceName: String = "mine",
        problem: String? = null,
    ): ThemeHealthEntry {
        return when (status) {
            EntryStatus.USABLE ->
                ThemeHealthEntry.Usable(SegmentKind.PREFIX, key, provenance, sourceName, "#123456", 700)
            EntryStatus.DISABLED ->
                ThemeHealthEntry.Disabled(SegmentKind.PREFIX, key, provenance, sourceName, "#123456", 700)
            EntryStatus.INVALID ->
                ThemeHealthEntry.Invalid(
                    SegmentKind.PREFIX,
                    key,
                    provenance,
                    sourceName,
                    null,
                    700,
                    problem ?: "invalid",
                )
        }
    }

    private enum class EntryStatus {
        USABLE,
        DISABLED,
        INVALID,
    }
}
