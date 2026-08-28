package dev.tailwindrainbow.intellij.adapter.theme

import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

/**
 * How another plugin adds themes to Tailwind Rainbow.
 *
 * Register an implementation against the `dev.tailwindrainbow.themeContributor` extension point.
 * Contributions are read the same way a user's own themes are: entries are validated leniently, a
 * malformed one is dropped and reported in the settings screen rather than thrown, and a theme may
 * name a [ThemeSpec.basedOn] to inherit every colour it does not set.
 *
 * A contributed theme is layered under the user's edits, so recolouring one in settings wins.
 */
fun interface ThemeContributor {
    fun themes(): List<ThemeSpec>
}
