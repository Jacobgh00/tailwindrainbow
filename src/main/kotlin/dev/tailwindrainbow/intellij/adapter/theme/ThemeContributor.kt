package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

/**
 * Adds themes to Tailwind Rainbow from another plugin, through the
 * `dev.tailwindrainbow.themeContributor` extension point.
 *
 * A spec may name a [ThemeSpec.basedOn] to inherit the colours it does not set. Malformed entries
 * are dropped and reported in the settings screen rather than thrown, and the user's own edits
 * override what is contributed.
 */
fun interface ThemeContributor {
    fun themes(): List<ThemeSpec>
}
