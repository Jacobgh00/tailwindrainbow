package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

enum class ThemeEntryProvenance {
    BUILT_IN,
    CONTRIBUTED,
    BASE,
    USER_OVERRIDE,
}

sealed interface ThemeHealthEntry {
    val section: SegmentKind
    val key: String
    val provenance: ThemeEntryProvenance
    val sourceName: String

    data class Usable(
        override val section: SegmentKind,
        override val key: String,
        override val provenance: ThemeEntryProvenance,
        override val sourceName: String,
        val color: String,
        val fontWeight: Int,
    ) : ThemeHealthEntry

    data class Disabled(
        override val section: SegmentKind,
        override val key: String,
        override val provenance: ThemeEntryProvenance,
        override val sourceName: String,
        val color: String,
        val fontWeight: Int,
    ) : ThemeHealthEntry

    data class Invalid(
        override val section: SegmentKind,
        override val key: String,
        override val provenance: ThemeEntryProvenance,
        override val sourceName: String,
        val color: String?,
        val fontWeight: Int?,
        val problem: String,
    ) : ThemeHealthEntry
}

data class ThemeHealthContext(
    val name: String,
    val effectiveTheme: RainbowTheme,
    val entries: List<ThemeHealthEntry>,
) {
    fun effectiveEntry(
        section: SegmentKind,
        key: String,
    ): ThemeHealthEntry? =
        entries.firstOrNull {
            it.section == section && it.key == key && it !is ThemeHealthEntry.Invalid
        }

    fun invalidEntries(
        section: SegmentKind,
        key: String,
    ): List<ThemeHealthEntry.Invalid> =
        entries.filterIsInstance<ThemeHealthEntry.Invalid>()
            .filter { it.section == section && it.key == key }

    fun entriesIn(section: SegmentKind): List<ThemeHealthEntry> = entries.filter { it.section == section }
}
