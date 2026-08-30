package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme
import dev.tailwindrainbow.intellij.domain.theme.overriddenBy

class ThemeRepository(sourceList: List<ThemeSource>) {
    constructor(vararg sources: ThemeSource) : this(sources.toList())

    private val sources = sourceList.toList()

    private val resolved: Map<String, RainbowTheme> by lazy {
        buildMap {
            sources.forEach { source ->
                source.themes().forEach { (name, theme) ->
                    put(name, this[name]?.overriddenBy(theme) ?: theme)
                }
            }
        }
    }

    init {
        require(sources.isNotEmpty()) { "a repository needs at least one source to fall back to" }
    }

    val names: Set<String> get() = resolved.keys

    fun find(name: String): RainbowTheme = resolved[name] ?: resolved.values.first()
}
