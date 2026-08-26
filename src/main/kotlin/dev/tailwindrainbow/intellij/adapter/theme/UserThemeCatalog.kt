package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.UserThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * Resolves theme names for the running plugin: built-in palettes with the user's overrides on top.
 *
 * Split out of the settings service, which was persisting preferences *and* composing palettes.
 * The repository is rebuilt only when the stored themes change, never per highlighting pass.
 */
class UserThemeCatalog : ThemeCatalog {
    @Volatile
    private var specs: List<ThemeSpec> = emptyList()

    @Volatile
    private var repository: ThemeRepository = build(emptyList())

    fun refresh(themes: List<ThemeSpec>) {
        specs = themes
        repository = build(themes)
    }

    override fun themeNamed(name: String): RainbowTheme = repository.find(name)

    fun names(): Set<String> = repository.names

    fun overrides(): List<ThemeSpec> = specs

    fun inherited(name: String): RainbowTheme = BuiltInThemes.themes()[name] ?: BuiltInThemes.default

    fun problems(): List<String> = UserThemeSource(specs).problems.map { "${it.themeName}: ${it.key} — ${it.message}" }

    private fun build(themes: List<ThemeSpec>) = ThemeRepository(BuiltInThemes, UserThemeSource(themes))
}
