package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.ThemeProblem
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

data class ThemeWorkspace(
    val themes: List<ThemeSpec> = emptyList(),
    val editing: String = "",
) {
    fun baseOf(name: String): String = specFor(name)?.basedOn ?: name

    fun specFor(name: String): ThemeSpec? = themes.firstOrNull { it.name == name }

    fun holding(edited: ThemeSpec?): Parked =
        if (editing.isEmpty()) {
            Parked(themes)
        } else {
            Parked(themes.filterNot { it.name == editing } + listOfNotNull(edited))
        }

    class Parked internal constructor(val themes: List<ThemeSpec>) {
        fun selecting(name: String): ThemeWorkspace = ThemeWorkspace(themes, name)

        fun creating(
            name: String,
            basedOn: String,
        ): ThemeWorkspace = reloading(themes + ThemeSpec(name, emptyList(), basedOn))

        fun duplicating(
            source: String,
            name: String,
        ): ThemeWorkspace = reloading(themes.duplicating(source, name))

        fun renaming(
            from: String,
            to: String,
        ): ThemeWorkspace = reloading(themes.renaming(from, to))

        fun removing(name: String): ThemeWorkspace = reloading(themes.filterNot { it.name == name })

        fun merging(imported: List<ThemeSpec>): ThemeWorkspace = reloading(themes.merging(imported))

        fun withoutEntriesFor(found: List<ThemeProblem>): ThemeWorkspace = reloading(themes.withoutEntriesFor(found))

        private fun reloading(themes: List<ThemeSpec>) = ThemeWorkspace(themes, editing = "")
    }
}
