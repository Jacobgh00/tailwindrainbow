package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.port.ThemeDefinitionSource
import dev.tailwindrainbow.intellij.application.port.ThemeHealthCatalog
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeCatalogSnapshot
import dev.tailwindrainbow.intellij.application.theme.ThemeEntryProvenance
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthContext
import dev.tailwindrainbow.intellij.application.theme.ThemeHealthLayer
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.themeCatalogSnapshotFor
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

class UserThemeCatalog(sources: List<ThemeSource> = emptyList()) : ThemeCatalog, ThemeHealthCatalog {
    constructor(vararg sources: ThemeSource) : this(sources.toList())

    private val stableSources = sources.toList()

    @Volatile
    private var snapshot = createSnapshot(emptyList())

    fun refresh(themes: List<ThemeSpec>) {
        snapshot = createSnapshot(themes)
    }

    override fun themeNamed(name: String): RainbowTheme = snapshot.themeNamed(name)

    override fun themeHealthNamed(name: String): ThemeHealthContext = snapshot.healthNamed(name)

    fun names(): Set<String> = snapshot.themes.keys

    fun baseNames(): Set<String> = snapshot.baseThemes.keys

    fun overrides(): List<ThemeSpec> = snapshot.specs

    fun basePalette(name: String): RainbowTheme = snapshot.basePalette(name)

    fun problems(): List<ThemeProblem> = snapshot.problems

    private fun createSnapshot(themes: List<ThemeSpec>): ThemeCatalogSnapshot {
        val baseLayers =
            listOf(
                ThemeHealthLayer.ResolvedThemes(
                    BuiltInThemes.themes(),
                    ThemeEntryProvenance.BUILT_IN,
                    BUILT_IN_SOURCE,
                ),
            ) + stableSources.map { source -> source.healthLayer() }

        return themeCatalogSnapshotFor(themes, baseLayers)
    }

    private fun ThemeSource.healthLayer(): ThemeHealthLayer =
        if (this is ThemeDefinitionSource) {
            ThemeHealthLayer.Specifications(specs(), ThemeEntryProvenance.CONTRIBUTED, sourceName)
        } else {
            ThemeHealthLayer.ResolvedThemes(themes(), ThemeEntryProvenance.CONTRIBUTED, ANONYMOUS_SOURCE)
        }

    private companion object {
        const val BUILT_IN_SOURCE = "built-in"
        const val ANONYMOUS_SOURCE = "contributed"
    }
}
