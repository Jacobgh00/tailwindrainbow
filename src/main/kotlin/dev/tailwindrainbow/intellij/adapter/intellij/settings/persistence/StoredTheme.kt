package dev.tailwindrainbow.intellij.adapter.intellij.settings.persistence

import com.intellij.util.xmlb.annotations.XCollection
import dev.tailwindrainbow.intellij.application.theme.StyleEntry
import dev.tailwindrainbow.intellij.application.theme.ThemeParser
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

/**
 * Serializable mirror of [ThemeSpec].
 *
 * Mutable with a no-arg constructor because that is what the IDE's XML serializer requires - which
 * is exactly why it is a separate type from the immutable domain model rather than an annotation
 * on it. Nothing here is validated; [dev.tailwindrainbow.intellij.application.theme.ThemeParser]
 * does that after [toSpec].
 */
class StoredTheme {
    var name: String = ""

    @get:XCollection(style = XCollection.Style.v2)
    var entries: MutableList<StoredStyle> = mutableListOf()

    fun toSpec(): ThemeSpec = ThemeSpec(name, entries.map(StoredStyle::toEntry))

    companion object {
        fun of(spec: ThemeSpec): StoredTheme =
            StoredTheme().apply {
                name = spec.name
                entries = spec.entries.mapTo(mutableListOf(), StoredStyle::of)
            }
    }
}

class StoredStyle {
    var section: SegmentKind = SegmentKind.PREFIX
    var key: String = ""
    var color: String = DEFAULT_COLOR
    var fontWeight: Int = FontWeight.BOLD.value
    var enabled: Boolean = true

    fun toEntry(): StyleEntry = StyleEntry(section, key, color, fontWeight, enabled)

    companion object {
        private const val DEFAULT_COLOR = "#ffffff"

        fun of(entry: StyleEntry): StoredStyle =
            StoredStyle().apply {
                section = entry.section
                key = entry.key
                color = entry.color
                fontWeight = entry.fontWeight
                enabled = entry.enabled
            }
    }
}
