package dev.tailwindrainbow.intellij.application.theme

fun problemsIntroducedBy(
    pending: List<ThemeSpec>,
    stored: List<ThemeSpec>,
): List<ThemeProblem> = SpecThemeSource(pending).problems - SpecThemeSource(stored).problems.toSet()

fun ThemeProblem.describe(): String {
    val entry = if (key.isBlank()) section.name.lowercase() else "${section.name.lowercase()} '$key'"

    return "$themeName: $entry — $message"
}
