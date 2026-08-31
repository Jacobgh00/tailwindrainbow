package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.overriddenBy

internal data class ThemeCatalogResolution(
    val themes: Map<String, RainbowTheme>,
    val entries: Map<String, List<ThemeHealthEntry>>,
) {
    fun healthContexts(): Map<String, ThemeHealthContext> =
        themes.mapValues { (name, theme) ->
            ThemeHealthContext(name, theme, entries[name].orEmpty())
        }
}

internal data class ThemeCatalogAppend(
    val catalog: ThemeCatalogResolution,
    val problems: List<ThemeProblem>,
)

internal fun resolveThemeLayers(layers: List<ThemeHealthLayer>): ThemeCatalogAppend =
    layers.fold(ThemeCatalogAppend(ThemeCatalogResolution(emptyMap(), emptyMap()), emptyList())) { resolved, layer ->
        val appended = resolved.catalog.append(layer)

        ThemeCatalogAppend(appended.catalog, resolved.problems + appended.problems)
    }

internal fun mergeThemes(layers: Iterable<Map<String, RainbowTheme>>): Map<String, RainbowTheme> =
    buildMap {
        layers.forEach { layer ->
            layer.forEach { (name, theme) ->
                put(name, this[name]?.overriddenBy(theme) ?: theme)
            }
        }
    }

internal fun ThemeCatalogResolution.append(layer: ThemeHealthLayer): ThemeCatalogAppend =
    when (layer) {
        is ThemeHealthLayer.ResolvedThemes -> appendResolved(layer)
        is ThemeHealthLayer.Specifications -> appendSpecifications(layer)
    }

private fun ThemeCatalogResolution.appendResolved(layer: ThemeHealthLayer.ResolvedThemes): ThemeCatalogAppend {
    val nextEntries = entries.toMutableMap()

    layer.themes.forEach { (name, theme) ->
        nextEntries[name] =
            mergeEntries(
                entries[name].orEmpty(),
                theme.healthEntries(layer.provenance, layer.sourceName),
            )
    }

    return ThemeCatalogAppend(
        catalog = ThemeCatalogResolution(mergeThemes(listOf(themes, layer.themes)), nextEntries),
        problems = emptyList(),
    )
}

private fun ThemeCatalogResolution.appendSpecifications(layer: ThemeHealthLayer.Specifications): ThemeCatalogAppend {
    val parsedSpecs = parseThemeSpecs(layer.specs)
    val specsByName = parsedSpecs.associateBy { it.spec.name }
    val layerThemes = resolveThemeSpecifications(specsByName.values, themes)

    val nextEntries = entries.toMutableMap()

    specsByName.forEach { (name, parsed) ->
        val entryMap = linkedMapOf<ThemeEntryKey, ThemeHealthEntry>()
        entryMap.putAll(entries[name].orEmpty())

        if (parsed.spec.basedOn in themes) {
            entries[parsed.spec.basedOn]
                .orEmpty()
                .filterNot { it is ThemeHealthEntry.Invalid }
                .map { it.asBase(parsed.spec.basedOn) }
                .let(entryMap::putAll)
        }

        entryMap.putAll(parsed.parsed.theme.healthEntries(layer.provenance, parsed.spec.name))
        entryMap.putAll(parsed.invalidEntries(layer.provenance, parsed.spec.name))
        nextEntries[name] = entryMap.values.toList()
    }

    return ThemeCatalogAppend(
        catalog = ThemeCatalogResolution(mergeThemes(listOf(themes, layerThemes)), nextEntries),
        problems = parsedSpecs.flatMap { it.parsed.problems },
    )
}

private fun ParsedThemeSpec.invalidEntries(
    provenance: ThemeEntryProvenance,
    sourceName: String,
): List<ThemeHealthEntry.Invalid> =
    parsed.problems.map { problem ->
        val raw = spec.entries.firstOrNull { it.section == problem.section && it.key == problem.key }

        ThemeHealthEntry.Invalid(
            section = problem.section,
            key = problem.key,
            provenance = provenance,
            sourceName = sourceName,
            color = raw?.color,
            fontWeight = raw?.fontWeight,
            problem = problem.message,
        )
    }

private fun mergeEntries(
    existing: List<ThemeHealthEntry>,
    additions: List<ThemeHealthEntry>,
): List<ThemeHealthEntry> {
    val byKey = linkedMapOf<ThemeEntryKey, ThemeHealthEntry>()
    byKey.putAll(existing)
    byKey.putAll(additions)
    return byKey.values.toList()
}

private fun MutableMap<ThemeEntryKey, ThemeHealthEntry>.putAll(entries: Iterable<ThemeHealthEntry>) {
    entries.forEach { entry -> put(ThemeEntryKey(entry.section, entry.key), entry) }
}

private data class ThemeEntryKey(
    val section: SegmentKind,
    val key: String,
)
