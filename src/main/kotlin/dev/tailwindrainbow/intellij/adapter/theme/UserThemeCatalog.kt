package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

class UserThemeCatalog(private val contributed: ThemeSource = ThemeSource { emptyMap() }) : ThemeCatalog {
    @Volatile
    private var specs: List<ThemeSpec> = emptyList()

    @Volatile
    private var source: SpecThemeSource = SpecThemeSource(emptyList(), BuiltInThemes)

    @Volatile
    private var repository: ThemeRepository = ThemeRepository(BuiltInThemes, contributed, source)

    @Volatile
    private var bases: ThemeRepository = ThemeRepository(BuiltInThemes, contributed)

    fun refresh(themes: List<ThemeSpec>) {
        specs = themes
        source = SpecThemeSource(themes, BuiltInThemes)
        repository = ThemeRepository(BuiltInThemes, contributed, source)
        bases = ThemeRepository(BuiltInThemes, contributed)
    }

    override fun themeNamed(name: String): RainbowTheme = repository.find(name)

    fun names(): Set<String> = repository.names

    fun baseNames(): Set<String> = bases.names

    fun overrides(): List<ThemeSpec> = specs

    fun basePalette(name: String): RainbowTheme = bases.find(name)

    fun problems(): List<ThemeProblem> = source.problems
}
