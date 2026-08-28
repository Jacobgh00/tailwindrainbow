package dev.tailwindrainbow.intellij.application.theme

/**
 * The problems [pending] would introduce that [stored] does not already have.
 *
 * Parsing is lenient — a malformed entry is dropped and reported rather than thrown — so a theme
 * file edited by hand can hold entries the plugin cannot use. Those must not stop the user saving
 * unrelated changes, which is why only what this edit adds is worth refusing.
 */
fun problemsIntroducedBy(
    pending: List<ThemeSpec>,
    stored: List<ThemeSpec>,
): List<ThemeProblem> = SpecThemeSource(pending).problems - SpecThemeSource(stored).problems.toSet()

/** One line naming the theme, the entry, and what is wrong with it. */
fun ThemeProblem.describe(): String {
    val entry = if (key.isBlank()) section.name.lowercase() else "${section.name.lowercase()} '$key'"

    return "$themeName: $entry — $message"
}
