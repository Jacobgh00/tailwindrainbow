package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.domain.theme.SegmentKind

val SegmentKind.displayName: String
    get() = name.lowercase().replaceFirstChar(Char::uppercaseChar)
