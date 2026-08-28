package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

/**
 * How a theme section is named on screen, next to [ThemeEditorRow.label] for the same reason: what
 * the user reads is decided here, where it can be tested, rather than in Swing.
 */
val SegmentKind.displayName: String
    get() = name.lowercase().replaceFirstChar(Char::uppercaseChar)
