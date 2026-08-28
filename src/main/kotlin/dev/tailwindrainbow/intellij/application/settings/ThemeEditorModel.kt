package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

/**
 * Where the style a row shows comes from.
 *
 * The three states are what tells [ThemeEditorModel.reset] and [ThemeEditorModel.remove] apart:
 * a row with an inherited value behind it is reset, one without is removed.
 */
enum class RowOrigin {
    INHERITED,
    OVERRIDDEN,
    ADDED,
}

data class ThemeEditorRow(
    val section: SegmentKind,
    val key: String,
    val color: String,
    val bold: Boolean,
    val origin: RowOrigin,
) {
    /** What to show in the token column. [key] is empty for the two keyless sections. */
    val label: String
        get() =
            when (section) {
                SegmentKind.ARBITRARY -> "[arbitrary]"
                SegmentKind.IMPORTANT -> "!important"
                else -> key
            }

    val isUserDefined: Boolean get() = origin != RowOrigin.INHERITED
}

/**
 * Holds the inherited palette and the user's overrides side by side so a row can show its
 * effective colour while still knowing whether the user chose it.
 *
 * Overrides stay sparse on purpose: only touched entries are stored, so a user who recolours one
 * prefix keeps inheriting every other colour — including ones added in later plugin versions.
 * A token the palette lacks is stored the same way, which is what lets a user colour a variant no
 * built-in theme knows about.
 */
class ThemeEditorModel private constructor(
    private val inherited: InheritedPalette,
    private val overrides: Map<EntryKey, StyleEntry>,
) {
    constructor(inherited: RainbowTheme, overrides: ThemeSpec? = null) : this(
        inherited = InheritedPalette(inherited),
        overrides = overrides?.entries.orEmpty().associateBy { EntryKey(it.section, it.key) },
    )

    fun rows(): List<ThemeEditorRow> {
        val inheritedEntries = inherited.entries()
        val addedKeys = overrides.keys - inheritedEntries.map { it.first }.toSet()

        return inheritedEntries.map { (entry, style) -> rowOf(entry, overrides[entry], style) } +
            addedKeys.map { rowOf(it, overrides[it], inheritedStyle = null) }
    }

    /** Whether a row for this token already exists, inherited or added. Asked before [add]. */
    fun holds(
        section: SegmentKind,
        key: String,
    ): Boolean {
        val entry = EntryKey(section, key)
        return entry in overrides || inherited.styleOf(entry) != null
    }

    /**
     * Adds a token the palette does not offer, in a neutral colour for the user to change. Only
     * sections addressed by key can take one; see [SegmentKind.isKeyed].
     */
    fun add(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel {
        require(section.isKeyed) { "a $section entry has no key to add" }
        require(key.isNotBlank()) { "a token needs a name" }
        require(!holds(section, key)) { "'$key' is already in the $section section" }

        val entry = EntryKey(section, key)
        return withOverrides(overrides + (entry to blankEntry(entry)))
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

    /** Drops the user's edit so the row follows the inherited palette again. */
    fun reset(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel {
        val entry = EntryKey(section, key)
        require(inherited.styleOf(entry) != null) { "'$key' has nothing to fall back to; remove it instead" }

        return withOverrides(overrides - entry)
    }

    /** Deletes an added row outright. A row the palette offers cannot be deleted, only [reset]. */
    fun remove(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel {
        val entry = EntryKey(section, key)
        require(inherited.styleOf(entry) == null) { "'$key' comes from the theme; reset it instead" }

        return withOverrides(overrides - entry)
    }

    /** The user's overrides only — never the inherited palette. */
    fun spec(name: String): ThemeSpec = ThemeSpec(name, overrides.values.toList())

    private fun withOverrides(overrides: Map<EntryKey, StyleEntry>) = ThemeEditorModel(inherited, overrides)

    private fun edit(
        entry: EntryKey,
        change: (StyleEntry) -> StyleEntry,
    ): ThemeEditorModel {
        val current = overrides[entry] ?: inherited.styleOf(entry)?.toEntry(entry) ?: blankEntry(entry)
        return withOverrides(overrides + (entry to change(current)))
    }
}

private const val BOLD = 700
private const val NORMAL = 400

/** What an added token starts out as, until the user picks a colour for it. */
private const val FALLBACK_COLOR = "#808080"

/**
 * Shapes one row from the two styles that may stand behind it. The user's [override] wins over the
 * [inheritedStyle], and which of the two is present is also what decides the row's origin.
 */
private fun rowOf(
    entry: EntryKey,
    override: StyleEntry?,
    inheritedStyle: TextStyle?,
): ThemeEditorRow {
    val effective = override ?: inheritedStyle?.toEntry(entry) ?: blankEntry(entry)

    return ThemeEditorRow(
        section = entry.section,
        key = entry.key,
        color = effective.color,
        bold = effective.fontWeight >= BOLD,
        origin =
            when {
                override == null -> RowOrigin.INHERITED
                inheritedStyle == null -> RowOrigin.ADDED
                else -> RowOrigin.OVERRIDDEN
            },
    )
}

private fun TextStyle.toEntry(entry: EntryKey) = StyleEntry(entry.section, entry.key, color, fontWeight.value, enabled)

private fun blankEntry(entry: EntryKey) = StyleEntry(entry.section, entry.key, FALLBACK_COLOR, BOLD)
