package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

data class ThemeSpec(
    val name: String,
    val entries: List<StyleEntry>,
    val basedOn: String = name,
) {
    val isRedundant: Boolean get() = entries.isEmpty() && basedOn == name
}

data class StyleEntry(
    val section: SegmentKind,
    val key: String,
    val color: String,
    val fontWeight: Int,
    val enabled: Boolean = true,
)

data class ThemeProblem(
    val themeName: String,
    val section: SegmentKind,
    val key: String,
    val message: String,
)
