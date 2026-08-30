package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

class UserThemeCatalog(sources: List<ThemeSource> = emptyList()) : ThemeCatalog {
    constructor(vararg sources: ThemeSource) : this(sources.toList())

    private val stableSources = sources.toList()

    @Volatile
    private var snapshot = createSnapshot(emptyList())

    fun refresh(themes: List<ThemeSpec>) {
        snapshot = createSnapshot(themes)
    }

    override fun themeNamed(name: String): RainbowTheme = snapshot.repository.find(name)

    fun names(): Set<String> = snapshot.repository.names

    fun baseNames(): Set<String> = snapshot.bases.names

    fun overrides(): List<ThemeSpec> = snapshot.specs

    fun basePalette(name: String): RainbowTheme = snapshot.bases.find(name)

    fun problems(): List<ThemeProblem> = snapshot.source.problems

    private fun createSnapshot(themes: List<ThemeSpec>): CatalogSnapshot {
        val specs = themes.map { it.copy(entries = it.entries.toList()) }
        val source = SpecThemeSource(specs, BuiltInThemes)

        return CatalogSnapshot(
            specs = specs,
            source = source,
            repository = ThemeRepository(listOf(BuiltInThemes) + stableSources + source),
            bases = ThemeRepository(listOf(BuiltInThemes) + stableSources),
        )
    }

    private data class CatalogSnapshot(
        val specs: List<ThemeSpec>,
        val source: SpecThemeSource,
        val repository: ThemeRepository,
        val bases: ThemeRepository,
    )
}
