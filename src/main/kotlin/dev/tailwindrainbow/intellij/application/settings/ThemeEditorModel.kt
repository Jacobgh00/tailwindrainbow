package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle
import dev.tailwindrainbow.intellij.domain.theme.isHexColor

enum class RowOrigin {
    INHERITED,
    OVERRIDDEN,
    ADDED,
}

data class RowStyle(
    val color: String,
    val bold: Boolean = true,
    val enabled: Boolean = true,
)

data class ThemeEditorRow(
    val section: SegmentKind,
    val key: String,
    val style: RowStyle,
    val origin: RowOrigin,
) {
    val label: String
        get() =
            when (section) {
                SegmentKind.ARBITRARY -> "[arbitrary]"
                SegmentKind.IMPORTANT -> "!important"
                else -> key
            }

    val isUserDefined: Boolean get() = origin != RowOrigin.INHERITED
}

class ThemeEditorModel private constructor(
    private val inherited: InheritedPalette,
    private val overrides: Map<EntryKey, StyleEntry>,
) {
    constructor(inherited: RainbowTheme, overrides: ThemeSpec? = null) : this(
        inherited = InheritedPalette(inherited),
        overrides = overrides?.entries.orEmpty().associateBy { EntryKey(it.section, it.key) },
    )

    fun rows(
        section: SegmentKind? = null,
        query: String = "",
    ): List<ThemeEditorRow> {
        val inheritedEntries = inherited.entries()
        val addedKeys = overrides.keys - inheritedEntries.map { it.first }.toSet()

        val rows =
            inheritedEntries.map { (entry, style) -> rowOf(entry, overrides[entry], style) } +
                addedKeys.map { rowOf(it, overrides[it], inheritedStyle = null) }

        return rows
            .filter { section == null || it.section == section }
            .filter { it.matches(query) }
            .sortedBy(ThemeEditorRow::section)
    }

    fun holds(
        section: SegmentKind,
        key: String,
    ): Boolean {
        val entry = EntryKey(section, key)
        return entry in overrides || inherited.styleOf(entry) != null
    }

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

    fun restyle(
        section: SegmentKind,
        key: String,
        style: RowStyle,
    ): ThemeEditorModel {
        val entry = EntryKey(section, key)
        return withOverrides(overrides + (entry to style.toEntry(entry)))
    }

    fun reset(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel {
        val entry = EntryKey(section, key)
        require(inherited.styleOf(entry) != null) { "'$key' has nothing to fall back to; remove it instead" }

        return withOverrides(overrides - entry)
    }

    fun remove(
        section: SegmentKind,
        key: String,
    ): ThemeEditorModel {
        val entry = EntryKey(section, key)
        require(inherited.styleOf(entry) == null) { "'$key' comes from the theme; reset it instead" }

        return withOverrides(overrides - entry)
    }

    fun spec(name: String): ThemeSpec = ThemeSpec(name, overrides.values.toList())

    fun palette(): RainbowTheme {
        val styles = rows().mapNotNull { row -> row.style.toTextStyle()?.let { row.section to (row.key to it) } }

        return RainbowTheme(
            prefix = styles.sectionOf(SegmentKind.PREFIX),
            base = styles.sectionOf(SegmentKind.BASE),
            arbitrary = styles.singleOf(SegmentKind.ARBITRARY),
            important = styles.singleOf(SegmentKind.IMPORTANT),
        )
    }

    private fun withOverrides(overrides: Map<EntryKey, StyleEntry>) = ThemeEditorModel(inherited, overrides)
}

private fun ThemeEditorRow.matches(query: String): Boolean {
    val wanted = query.trim()

    return wanted.isEmpty() || label.contains(wanted, ignoreCase = true)
}

private fun rowOf(
    entry: EntryKey,
    override: StyleEntry?,
    inheritedStyle: TextStyle?,
): ThemeEditorRow {
    val effective = override ?: inheritedStyle?.toEntry(entry) ?: blankEntry(entry)

    return ThemeEditorRow(
        section = entry.section,
        key = entry.key,
        style =
            RowStyle(
                effective.color,
                bold = effective.fontWeight >= FontWeight.BOLD.value,
                enabled = effective.enabled,
            ),
        origin =
            when {
                override == null -> RowOrigin.INHERITED
                inheritedStyle == null -> RowOrigin.ADDED
                else -> RowOrigin.OVERRIDDEN
            },
    )
}

private typealias SectionedStyles = List<Pair<SegmentKind, Pair<String, TextStyle>>>

private fun SectionedStyles.sectionOf(section: SegmentKind) = filter { it.first == section }.associate { it.second }

private fun SectionedStyles.singleOf(section: SegmentKind) = firstOrNull { it.first == section }?.second?.second

private fun RowStyle.toTextStyle(): TextStyle? =
    if (color.isHexColor()) {
        TextStyle(color, FontWeight.of(if (bold) FontWeight.BOLD.value else FontWeight.NORMAL.value), enabled)
    } else {
        null
    }

private fun TextStyle.toEntry(entry: EntryKey) = StyleEntry(entry.section, entry.key, color, fontWeight.value, enabled)

private fun RowStyle.toEntry(entry: EntryKey) = StyleEntry(entry.section, entry.key, color, weight(), enabled)

private fun RowStyle.weight() = if (bold) FontWeight.BOLD.value else FontWeight.NORMAL.value

private fun blankEntry(entry: EntryKey) = newThemeEntry(entry.section, entry.key)
