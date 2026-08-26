package dev.tailwindrainbow.intellij.application.port

import dev.tailwindrainbow.intellij.application.theme.ThemeRepository
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * A place themes come from.
 *
 * Implementations are ordered by [ThemeRepository]: later sources override earlier ones entry by
 * entry, so a user source can retint one prefix of a built-in theme without restating the rest.
 */
fun interface ThemeSource {
    fun themes(): Map<String, RainbowTheme>
}

