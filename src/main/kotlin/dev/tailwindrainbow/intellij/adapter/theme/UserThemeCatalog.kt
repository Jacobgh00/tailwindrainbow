package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.port.ThemeCatalog
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.application.theme.UserThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * The repository is rebuilt only when the stored themes change, never per highlighting pass.
 */
class UserThemeCatalog : ThemeCatalog {
    @Volatile
    private var specs: List<ThemeSpec> = emptyList()

    @Volatile
    private var source: UserThemeSource = UserThemeSource(emptyList(), BuiltInThemes)

    @Volatile
    private var repository: ThemeRepository = ThemeRepository(BuiltInThemes, source)

    fun refresh(themes: List<ThemeSpec>) {
        specs = themes
        source = UserThemeSource(themes, BuiltInThemes)
        repository = ThemeRepository(BuiltInThemes, source)
    }

    override fun themeNamed(name: String): RainbowTheme = repository.find(name)

    fun names(): Set<String> = repository.names

    /** The themes a user theme can be based on. */
    fun builtInNames(): Set<String> = BuiltInThemes.themes().keys

    fun overrides(): List<ThemeSpec> = specs

    /**
     * The palette a theme derived from [name] sits on top of: built-ins only.
     *
     * Not [themeNamed], which returns the merged result — the editor must show what would remain
     * if the user reset a row, so it cannot include the user's own edits.
     */
    fun builtIn(name: String): RainbowTheme = BuiltInThemes.themes()[name] ?: BuiltInThemes.default

    /** What the stored themes hold that the plugin cannot use. Raw: the settings screen words them. */
    fun problems(): List<ThemeProblem> = source.problems
}
