package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeDefinitionSource
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeEntryProvenance
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.variants.VariantDeclaration
import dev.tailwindrainbow.intellij.application.variants.VariantDeclarationKind
import dev.tailwindrainbow.intellij.application.variants.VariantHealthAnalyzer
import dev.tailwindrainbow.intellij.application.variants.VariantScanResult
import dev.tailwindrainbow.intellij.application.variants.VariantSourceLocation
import dev.tailwindrainbow.intellij.application.variants.VariantStatus
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UserThemeCatalogTest {
    private val contributedStyle = TextStyle("#010101", FontWeight.BOLD)
    private val contributed =
        ThemeSource {
            mapOf(
                "corporate" to
                    RainbowTheme(
                        prefix =
                            mapOf(
                                "hover" to contributedStyle,
                                "contributed-only" to contributedStyle,
                            ),
                    ),
            )
        }
    private val contributedDefinitions =
        object : ThemeSource, ThemeDefinitionSource {
            private val definitions =
                listOf(
                    ThemeSpec(
                        "corporate",
                        listOf(
                            StyleEntry(
                                SegmentKind.PREFIX,
                                "hover",
                                contributedStyle.color,
                                contributedStyle.fontWeight.value,
                            ),
                            StyleEntry(
                                SegmentKind.PREFIX,
                                "contributed-only",
                                contributedStyle.color,
                                contributedStyle.fontWeight.value,
                            ),
                        ),
                        basedOn = BuiltInThemes.DEFAULT_NAME,
                    ),
                )

            override val sourceName: String = "test contributor"

            override fun specs(): List<ThemeSpec> = definitions

            override fun themes(): Map<String, RainbowTheme> = SpecThemeSource(definitions, BuiltInThemes).themes()
        }

    @Test
    fun `a contributed theme is offered alongside the built-ins`() {
        val catalog = UserThemeCatalog(contributed)

        assertTrue(catalog.names().containsAll(BuiltInThemes.themes().keys + "corporate"))
    }

    @Test
    fun `a snapshot reads each source once and shares it with health`() {
        var calls = 0
        val source =
            ThemeSource {
                calls++
                mapOf("dynamic" to RainbowTheme(prefix = mapOf("hover" to TextStyle("#010101", FontWeight.BOLD))))
            }
        val catalog = UserThemeCatalog(source)
        calls = 0

        catalog.refresh(emptyList())

        assertEquals(1, calls)
        assertEquals(catalog.themeNamed("dynamic"), catalog.themeHealthNamed("dynamic").effectiveTheme)
        assertIs<ThemeHealthEntry.Usable>(
            catalog.themeHealthNamed("dynamic").effectiveEntry(SegmentKind.PREFIX, "hover"),
        )
    }

    @Test
    fun `the user's own colour wins over the one that was contributed`() {
        val catalog = UserThemeCatalog(contributed)

        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("hover", "#020202")))))

        assertEquals("#020202", catalog.themeNamed("corporate").prefix.getValue("hover").color)
    }

    @Test
    fun `an untouched entry of a contributed theme keeps its contributed colour`() {
        val catalog = UserThemeCatalog(contributed)

        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("focus", "#020202")))))

        assertEquals(contributedStyle, catalog.themeNamed("corporate").prefix["hover"])
    }

    @Test
    fun `the palette a row falls back to includes contributed themes`() {
        val catalog = UserThemeCatalog(contributed)
        catalog.refresh(listOf(ThemeSpec("corporate", listOf(entry("hover", "#020202")))))

        assertEquals(
            contributedStyle,
            catalog.basePalette("corporate").prefix["hover"],
            "resetting the row must offer the contributed colour, not the built-in one",
        )
    }

    @Test
    fun `a contributed theme cannot be deleted, so it counts as a base`() {
        assertTrue("corporate" in UserThemeCatalog(contributed).baseNames())
    }

    @Test
    fun `refresh copies the caller's theme list`() {
        val themes = mutableListOf(ThemeSpec("local", listOf(entry("hover", "#020202"))))
        val catalog = UserThemeCatalog()

        catalog.refresh(themes)
        themes.clear()

        assertEquals("local", catalog.overrides().single().name)
        assertTrue("local" in catalog.names())
    }

    @Test
    fun `refresh copies the entries inside the caller's theme list`() {
        val entries = mutableListOf(entry("hover", "#020202"))
        val catalog = UserThemeCatalog()

        catalog.refresh(listOf(ThemeSpec("local", entries)))
        entries.clear()

        assertEquals(1, catalog.overrides().single().entries.size)
    }

    @Test
    fun `a previously published view remains from its original generation`() {
        val catalog = UserThemeCatalog()
        val first = ThemeSpec("first", listOf(entry("hover", "#020202")))
        val second = ThemeSpec("second", listOf(entry("hover", "#030303")))

        catalog.refresh(listOf(first))
        val firstOverrides = catalog.overrides()

        catalog.refresh(listOf(second))

        assertEquals(listOf(first), firstOverrides)
        assertEquals(listOf(second), catalog.overrides())
        assertTrue("second" in catalog.names())
        assertTrue("first" !in catalog.names())
    }

    @Test
    fun `health retains user provenance and inherited base provenance`() {
        val catalog = UserThemeCatalog()
        catalog.refresh(listOf(ThemeSpec("default", listOf(entry("hover", "#020202")))))

        val health = catalog.themeHealthNamed("default")

        assertEquals(
            ThemeEntryProvenance.USER_OVERRIDE,
            health.effectiveEntry(SegmentKind.PREFIX, "hover")?.provenance,
        )
        assertEquals(
            ThemeEntryProvenance.BASE,
            health.effectiveEntry(SegmentKind.PREFIX, "focus")?.provenance,
        )
    }

    @Test
    fun `health identifies built-in and contributed winning entries`() {
        val catalog = UserThemeCatalog(contributed)

        assertEquals(
            ThemeEntryProvenance.BUILT_IN,
            catalog.themeHealthNamed("default").effectiveEntry(SegmentKind.PREFIX, "hover")?.provenance,
        )
        assertEquals(
            ThemeEntryProvenance.CONTRIBUTED,
            catalog.themeHealthNamed("corporate").effectiveEntry(SegmentKind.PREFIX, "hover")?.provenance,
        )
    }

    @Test
    fun `health keeps a contributed theme's inherited entries as base entries`() {
        val catalog = UserThemeCatalog(contributedDefinitions)
        val health = catalog.themeHealthNamed("corporate")

        assertEquals(
            ThemeEntryProvenance.CONTRIBUTED,
            health.effectiveEntry(SegmentKind.PREFIX, "hover")?.provenance,
        )
        assertEquals(
            ThemeEntryProvenance.BASE,
            health.effectiveEntry(SegmentKind.PREFIX, "focus")?.provenance,
        )
    }

    @Test
    fun `a user theme can inherit a contributed base and reports that provenance`() {
        val catalog = UserThemeCatalog(contributed)
        catalog.refresh(listOf(ThemeSpec("mine", emptyList(), basedOn = "corporate")))

        assertEquals(contributedStyle, catalog.themeNamed("mine").prefix["hover"])
        assertEquals(
            ThemeEntryProvenance.BASE,
            catalog.themeHealthNamed("mine").effectiveEntry(SegmentKind.PREFIX, "hover")?.provenance,
        )
    }

    @Test
    fun `health retains external entries that survive a user theme merge`() {
        val catalog = UserThemeCatalog(contributed)
        catalog.refresh(listOf(ThemeSpec("corporate", emptyList(), basedOn = "default")))
        val entry =
            checkNotNull(catalog.themeHealthNamed("corporate").effectiveEntry(SegmentKind.PREFIX, "contributed-only"))

        assertEquals(contributedStyle.color, assertIs<ThemeHealthEntry.Usable>(entry).color)
        assertEquals(ThemeEntryProvenance.CONTRIBUTED, entry.provenance)
    }

    @Test
    fun `health analyzer sees malformed wildcard entries from the catalog`() {
        val catalog = UserThemeCatalog()
        catalog.refresh(
            listOf(
                ThemeSpec(
                    "mine",
                    listOf(StyleEntry(SegmentKind.PREFIX, "project-*", "not-a-colour", 700)),
                    basedOn = "missing-base",
                ),
            ),
        )
        val declaration =
            VariantDeclaration(
                "supports-grid",
                VariantDeclarationKind.CUSTOM_VARIANT,
                VariantSourceLocation("styles.css", 0, 1),
            )

        val assessment =
            VariantHealthAnalyzer(catalog)
                .analyze(
                    "mine",
                    VariantScanResult(listOf(declaration.copy(name = "project-grid")), 0),
                )
                .assessments
                .single()

        assertIs<VariantStatus.Invalid>(assessment.status)
    }

    @Test
    fun `health keeps malformed and disabled user entries distinct`() {
        val catalog = UserThemeCatalog()
        catalog.refresh(
            listOf(
                ThemeSpec(
                    "default",
                    listOf(
                        StyleEntry(SegmentKind.PREFIX, "hover", "not-a-colour", 700),
                        StyleEntry(SegmentKind.PREFIX, "focus", "#020202", 700, enabled = false),
                    ),
                ),
            ),
        )

        val health = catalog.themeHealthNamed("default")

        assertIs<ThemeHealthEntry.Invalid>(
            health.invalidEntries(SegmentKind.PREFIX, "hover").single(),
        )
        assertIs<ThemeHealthEntry.Disabled>(
            health.effectiveEntry(SegmentKind.PREFIX, "focus"),
        )
    }

    @Test
    fun `an unknown theme name falls back to the default theme`() {
        val catalog = UserThemeCatalog()

        assertEquals(catalog.themeNamed(BuiltInThemes.DEFAULT_NAME), catalog.themeNamed("no-such-theme"))
    }

    @Test
    fun `a catalog copies its source list, so a later edit cannot change what it resolves`() {
        val sources = mutableListOf<ThemeSource>(contributed)
        val catalog = UserThemeCatalog(sources)

        sources.clear()
        catalog.refresh(emptyList())

        assertEquals(contributedStyle, catalog.themeNamed("corporate").prefix["contributed-only"])
    }

    private fun entry(
        key: String,
        color: String,
    ) = StyleEntry(SegmentKind.PREFIX, key, color, FontWeight.BOLD.value)
}
