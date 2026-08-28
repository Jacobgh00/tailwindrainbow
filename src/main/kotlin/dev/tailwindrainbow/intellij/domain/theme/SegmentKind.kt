package dev.tailwindrainbow.intellij.domain.theme

/** Which section of a [RainbowTheme] produced a match. Decided by [ThemeMatcher], never inferred. */
enum class SegmentKind {
    PREFIX,
    BASE,
    ARBITRARY,
    IMPORTANT,
    ;

    /**
     * Whether the section holds many entries addressed by key.
     *
     * [ARBITRARY] and [IMPORTANT] hold a single style each, so a key would have nothing to name.
     * The distinction decides what a theme may contain, so it belongs to the model rather than to
     * each of the places that has to respect it.
     */
    val isKeyed: Boolean get() = this == PREFIX || this == BASE
}
