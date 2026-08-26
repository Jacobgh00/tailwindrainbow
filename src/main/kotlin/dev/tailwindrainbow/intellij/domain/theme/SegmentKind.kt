package dev.tailwindrainbow.intellij.domain.theme

/** Which section of a [RainbowTheme] produced a match. Decided by [ThemeMatcher], never inferred. */
enum class SegmentKind {
    PREFIX,
    BASE,
    ARBITRARY,
    IMPORTANT,
}
