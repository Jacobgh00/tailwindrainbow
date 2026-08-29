package dev.tailwindrainbow.intellij.application.theme

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import dev.tailwindrainbow.intellij.domain.theme.FontWeight
import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

fun themeFromFile(json: String): ThemeSpec? {
    val root = runCatching { JsonParser.parseString(json).asJsonObject }.getOrNull() ?: return null
    val name = root.keySet().firstOrNull() ?: return null
    val sections = root.getAsJsonObjectOrNull(name) ?: return null

    return ThemeSpec(name, sections.toEntries())
}

private fun JsonObject.toEntries(): List<StyleEntry> =
    buildList {
        addAll(keyedEntries(PREFIX, SegmentKind.PREFIX))
        addAll(keyedEntries(BASE, SegmentKind.BASE))
        singleEntry(ARBITRARY, SegmentKind.ARBITRARY)?.let(::add)
        singleEntry(IMPORTANT, SegmentKind.IMPORTANT)?.let(::add)
    }

private fun JsonObject.keyedEntries(
    section: String,
    kind: SegmentKind,
): List<StyleEntry> {
    val entries = getAsJsonObjectOrNull(section) ?: return emptyList()

    return entries.keySet().mapNotNull { key -> entries.getAsJsonObjectOrNull(key)?.toEntry(kind, key) }
}

private fun JsonObject.singleEntry(
    section: String,
    kind: SegmentKind,
): StyleEntry? = getAsJsonObjectOrNull(section)?.toEntry(kind, "")

private fun JsonObject.toEntry(
    kind: SegmentKind,
    key: String,
): StyleEntry =
    StyleEntry(
        section = kind,
        key = key,
        color = get(COLOR)?.asStringOrNull().orEmpty(),
        fontWeight = weightOf(get(FONT_WEIGHT)?.asStringOrNull()),
        enabled = get(ENABLED)?.let { runCatching { it.asBoolean }.getOrDefault(true) } ?: true,
    )

private fun JsonObject.getAsJsonObjectOrNull(key: String): JsonObject? {
    return runCatching { getAsJsonObject(key) }.getOrNull()
}

private fun JsonElement.asStringOrNull() = runCatching { asString }.getOrNull()

private fun weightOf(value: String?): Int {
    val named = value?.lowercase() ?: return FontWeight.NORMAL.value

    return WEIGHT_NAMES[named] ?: named.toIntOrNull() ?: FontWeight.NORMAL.value
}
