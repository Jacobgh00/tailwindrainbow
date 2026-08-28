package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

/** Addresses one style of a theme: which section it lives in, and under which name. */
data class EntryKey(val section: SegmentKind, val key: String)

/**
 * The palette an editor sits on top of — what the theme offers before the user changes anything.
 *
 * Exists so [ThemeEditorModel] deals in one flat list of [EntryKey]s rather than in four differently
 * shaped sections of [RainbowTheme].
 */
internal class InheritedPalette(private val theme: RainbowTheme) {
    /** Every style the theme offers, in the order the editor lists them. */
    fun entries(): List<Pair<EntryKey, TextStyle>> =
        buildList {
            theme.prefix.forEach { (key, style) -> add(EntryKey(SegmentKind.PREFIX, key) to style) }
            theme.base.forEach { (key, style) -> add(EntryKey(SegmentKind.BASE, key) to style) }
            theme.arbitrary?.let { add(EntryKey(SegmentKind.ARBITRARY, "") to it) }
            theme.important?.let { add(EntryKey(SegmentKind.IMPORTANT, "") to it) }
        }

    /** The style behind an entry, or null when the theme has nothing to say about it. */
    fun styleOf(entry: EntryKey): TextStyle? =
        when (entry.section) {
            SegmentKind.PREFIX -> theme.prefix[entry.key]
            SegmentKind.BASE -> theme.base[entry.key]
            SegmentKind.ARBITRARY -> theme.arbitrary
            SegmentKind.IMPORTANT -> theme.important
        }
}
