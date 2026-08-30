package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

internal const val ADDED_TOKEN_COLOR = "#808080"

internal fun newThemeEntry(
    section: SegmentKind,
    key: String,
): StyleEntry = StyleEntry(section, key, ADDED_TOKEN_COLOR, FontWeight.BOLD.value)

fun List<ThemeSpec>.duplicating(
    source: String,
    name: String,
): List<ThemeSpec> {
    val original = firstOrNull { it.name == source }

    return this + (original?.copy(name = name) ?: ThemeSpec(name, emptyList(), basedOn = source))
}

fun List<ThemeSpec>.renaming(
    from: String,
    to: String,
): List<ThemeSpec> =
    map { spec ->
        spec.copy(
            name = if (spec.name == from) to else spec.name,
            basedOn = if (spec.basedOn == from) to else spec.basedOn,
        )
    }

fun List<ThemeSpec>.merging(imported: List<ThemeSpec>): List<ThemeSpec> =
    filterNot { existing -> imported.any { it.name == existing.name } } + imported

fun List<ThemeSpec>.addingEntry(
    themeName: String,
    entry: StyleEntry,
): List<ThemeSpec> {
    val existing = firstOrNull { it.name == themeName }
    val updated = existing?.copy(entries = existing.entries.replacing(entry)) ?: ThemeSpec(themeName, listOf(entry))

    return filterNot { it.name == themeName } + updated
}

fun List<ThemeSpec>.withoutEntriesFor(problems: List<ThemeProblem>): List<ThemeSpec> =
    map { spec ->
        spec.copy(
            entries =
                spec.entries.filterNot { entry ->
                    problems.any { it.themeName == spec.name && it.section == entry.section && it.key == entry.key }
                },
        )
    }

private fun List<StyleEntry>.replacing(entry: StyleEntry): List<StyleEntry> =
    filterNot { it.section == entry.section && it.key == entry.key } + entry
