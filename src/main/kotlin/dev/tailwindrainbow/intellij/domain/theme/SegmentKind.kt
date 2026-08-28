package dev.tailwindrainbow.intellij.domain.theme

enum class SegmentKind {
    PREFIX,
    BASE,
    ARBITRARY,
    IMPORTANT,
    ;

    val isKeyed: Boolean get() = this == PREFIX || this == BASE
}
