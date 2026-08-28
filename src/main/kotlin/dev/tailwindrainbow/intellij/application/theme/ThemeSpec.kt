package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

data class ThemeSpec(
    val name: String,
    val entries: List<StyleEntry>,
    /**
     * The theme this one is layered on. A spec that restyles an existing theme names itself; one
     * the user created names the theme it was started from.
     */
    val basedOn: String = name,
) {
    /** True when the spec neither restyles anything nor names a theme that would not otherwise exist. */
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
