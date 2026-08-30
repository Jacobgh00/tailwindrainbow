package dev.tailwindrainbow.intellij.application.settings

import dev.tailwindrainbow.intellij.application.theme.ThemeSpec

data class ThemeWorkspace(
    val themes: List<ThemeSpec>,
    val editing: String? = null,
) {
    fun commit(draft: ThemeSpec?): ThemeWorkspace {
        val name = editing ?: return this
        val committed = themes.filterNot { it.name == name } + listOfNotNull(draft)

        return copy(themes = committed)
    }

    fun select(
        name: String,
        draft: ThemeSpec?,
    ): ThemeWorkspace = commit(draft).copy(editing = name)

    fun selectedTheme(): ThemeSpec? = editing?.let { name -> themes.firstOrNull { it.name == name } }

    fun baseOf(name: String): String = themes.firstOrNull { it.name == name }?.basedOn ?: name

    companion object {
        fun load(themes: List<ThemeSpec>): ThemeWorkspace = ThemeWorkspace(themes.toList())
    }
}
