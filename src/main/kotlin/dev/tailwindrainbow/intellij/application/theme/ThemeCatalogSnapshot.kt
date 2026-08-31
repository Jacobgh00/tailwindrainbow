package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

data class ThemeCatalogSnapshot(
    val specs: List<ThemeSpec>,
    val themes: Map<String, RainbowTheme>,
    val baseThemes: Map<String, RainbowTheme>,
    val problems: List<ThemeProblem>,
    val health: Map<String, ThemeHealthContext>,
) {
    init {
        require(themes.isNotEmpty()) { "a theme catalog needs at least one theme" }
        require(baseThemes.isNotEmpty()) { "a theme catalog needs at least one base theme" }
    }

    fun themeNamed(name: String): RainbowTheme = themes[name] ?: themes.values.first()

    fun healthNamed(name: String): ThemeHealthContext = health[name] ?: health.values.first()

    fun basePalette(name: String): RainbowTheme = baseThemes[name] ?: baseThemes.values.first()
}

fun themeCatalogSnapshotFor(
    specs: List<ThemeSpec>,
    baseLayers: List<ThemeHealthLayer>,
): ThemeCatalogSnapshot {
    require(baseLayers.isNotEmpty()) { "a theme catalog needs at least one base layer" }

    val copiedSpecs = specs.map { it.copy(entries = it.entries.toList()) }
    val base = resolveThemeLayers(baseLayers)
    val all =
        base.catalog.append(
            ThemeHealthLayer.Specifications(
                specs = copiedSpecs,
                provenance = ThemeEntryProvenance.USER_OVERRIDE,
                sourceName = USER_SOURCE,
            ),
        )

    return ThemeCatalogSnapshot(
        specs = copiedSpecs,
        themes = all.catalog.themes,
        baseThemes = base.catalog.themes,
        problems = base.problems + all.problems,
        health = all.catalog.healthContexts(),
    )
}

private const val USER_SOURCE = "user"
