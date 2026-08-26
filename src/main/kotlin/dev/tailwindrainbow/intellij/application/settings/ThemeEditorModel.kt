package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

/** One editable line in the theme editor: what it currently looks like, and whether the user set it. */
data class ThemeEditorRow(
    val section: SegmentKind,
    val key: String,
    val color: String,
    val bold: Boolean,
    val overridden: Boolean,
) {
    /** What to show in the token column. [key] is empty for the two keyless sections. */
    val label: String
        get() =
            when (section) {
                SegmentKind.ARBITRARY -> "[arbitrary]"
                SegmentKind.IMPORTANT -> "!important"
                else -> key
            }
}

/**
 * The theme editor's logic, with no widgets in sight.
 *
 * Holds the inherited palette and the user's overrides side by side so a row can show its effective
 * colour while still knowing whether the user chose it. Every edit returns a new model, so the table
 * can rebuild from a value rather than mutating shared state.
 *
 * Overrides stay sparse on purpose: only touched entries are stored, so a user who recolours one
 * prefix keeps inheriting every other colour — including ones added in later plugin versions.
 */
class ThemeEditorModel private constructor(
    private val inherited: RainbowTheme,
    private val overrides: Map<EntryKey, StyleEntry>,
) {
    constructor(inherited: RainbowTheme, overrides: ThemeSpec? = null) : this(
        inherited = inherited,
        overrides = overrides?.entries.orEmpty().associateBy { EntryKey(it.section, it.key) },
    )

    fun rows(): List<ThemeEditorRow> {
        val inheritedKeys = inheritedEntries().map { it.first }
        val strayOverrides = overrides.keys.filterNot { it in inheritedKeys }

        return inheritedEntries().map { (key, style) -> row(key, style) } +
            strayOverrides.map { row(it, inheritedStyle = null) }
    }

    fun recolour(
        section: SegmentKind,
        key: String,
        color: String,
    ): ThemeEditorModel = edit(EntryKey(section, key)) { it.copy(color = color) }

    fun setBold(
        section: SegmentKind,
        key: String,
        bold: Boolean,
    ): ThemeEditorModel = edit(EntryKey(section, key)) { it.copy(fontWeight = if (bold) BOLD else NORMAL) }

    fun reset(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel = ThemeEditorModel(inherited, overrides - EntryKey(section, key))

    /** The user's overrides only — never the inherited palette. */
    fun spec(name: String): ThemeSpec = ThemeSpec(name, overrides.values.toList())

    private fun edit(
        key: EntryKey,
        change: (StyleEntry) -> StyleEntry,
    ): ThemeEditorModel {
        val current = overrides[key] ?: inheritedStyle(key)?.toEntry(key) ?: blankEntry(key)
        return ThemeEditorModel(inherited, overrides + (key to change(current)))
    }

    private fun row(
        key: EntryKey,
        inheritedStyle: TextStyle?,
    ): ThemeEditorRow {
        val override = overrides[key]
        val effective = override ?: inheritedStyle?.toEntry(key) ?: blankEntry(key)

        return ThemeEditorRow(
            section = key.section,
            key = key.key,
            color = effective.color,
            bold = effective.fontWeight >= BOLD,
            overridden = override != null,
        )
    }

    private fun inheritedEntries(): List<Pair<EntryKey, TextStyle>> =
        buildList {
            inherited.prefix.forEach { (key, style) -> add(EntryKey(SegmentKind.PREFIX, key) to style) }
            inherited.base.forEach { (key, style) -> add(EntryKey(SegmentKind.BASE, key) to style) }
            inherited.arbitrary?.let { add(EntryKey(SegmentKind.ARBITRARY, "") to it) }
            inherited.important?.let { add(EntryKey(SegmentKind.IMPORTANT, "") to it) }
        }

    private fun inheritedStyle(key: EntryKey): TextStyle? =
        when (key.section) {
            SegmentKind.PREFIX -> inherited.prefix[key.key]
            SegmentKind.BASE -> inherited.base[key.key]
            SegmentKind.ARBITRARY -> inherited.arbitrary
            SegmentKind.IMPORTANT -> inherited.important
        }

    data class EntryKey(val section: SegmentKind, val key: String)

    private companion object {
        const val BOLD = 700
        const val NORMAL = 400
        const val FALLBACK_COLOR = "#808080"

        fun TextStyle.toEntry(key: EntryKey) = StyleEntry(key.section, key.key, color, fontWeight.value, enabled)

        fun blankEntry(key: EntryKey) = StyleEntry(key.section, key.key, FALLBACK_COLOR, BOLD)
    }
}
