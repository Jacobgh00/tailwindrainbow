package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind
import dev.tailwindrainbow.intellij.domain.theme.TextStyle

data class EntryKey(val section: SegmentKind, val key: String)

internal class InheritedPalette(private val theme: RainbowTheme) {
    fun entries(): List<Pair<EntryKey, TextStyle>> =
        buildList {
            theme.prefix.forEach { (key, style) -> add(EntryKey(SegmentKind.PREFIX, key) to style) }
            theme.base.forEach { (key, style) -> add(EntryKey(SegmentKind.BASE, key) to style) }
            theme.arbitrary?.let { add(EntryKey(SegmentKind.ARBITRARY, "") to it) }
            theme.important?.let { add(EntryKey(SegmentKind.IMPORTANT, "") to it) }
        }

    fun styleOf(entry: EntryKey): TextStyle? =
        when (entry.section) {
            SegmentKind.PREFIX -> theme.prefix[entry.key]
            SegmentKind.BASE -> theme.base[entry.key]
            SegmentKind.ARBITRARY -> theme.arbitrary
            SegmentKind.IMPORTANT -> theme.important
        }
}
