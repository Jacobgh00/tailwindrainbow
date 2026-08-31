package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

internal fun ThemeHealthEntry.asBase(baseName: String): ThemeHealthEntry =
    when (this) {
        is ThemeHealthEntry.Usable -> copy(provenance = ThemeEntryProvenance.BASE, sourceName = baseName)
        is ThemeHealthEntry.Disabled -> copy(provenance = ThemeEntryProvenance.BASE, sourceName = baseName)
        is ThemeHealthEntry.Invalid -> this
    }

internal fun RainbowTheme.healthEntries(
    provenance: ThemeEntryProvenance,
    sourceName: String,
): List<ThemeHealthEntry> =
    buildList {
        addAll(prefix.healthEntries(SegmentKind.PREFIX, provenance, sourceName))
        addAll(base.healthEntries(SegmentKind.BASE, provenance, sourceName))
        arbitrary?.let { add(it.healthEntry(SegmentKind.ARBITRARY, "arbitrary", provenance, sourceName)) }
        important?.let { add(it.healthEntry(SegmentKind.IMPORTANT, "important", provenance, sourceName)) }
    }

private fun Map<String, TextStyle>.healthEntries(
    section: SegmentKind,
    provenance: ThemeEntryProvenance,
    sourceName: String,
): List<ThemeHealthEntry> = entries.map { (key, style) -> style.healthEntry(section, key, provenance, sourceName) }

private fun TextStyle.healthEntry(
    section: SegmentKind,
    key: String,
    provenance: ThemeEntryProvenance,
    sourceName: String,
): ThemeHealthEntry =
    if (enabled) {
        ThemeHealthEntry.Usable(section, key, provenance, sourceName, color, fontWeight.value)
    } else {
        ThemeHealthEntry.Disabled(section, key, provenance, sourceName, color, fontWeight.value)
    }
