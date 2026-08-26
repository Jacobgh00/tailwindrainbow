package dev.tailwindrainbow.intellij.application.theme

import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

/**
 * Resolves a theme name against every source, in order.
 *
 * Sources later in [sources] override earlier ones **entry by entry**, so a user source that sets
 * only `hover` keeps every other color of the built-in theme it shadows. Overriding by whole theme
 * instead would force users to restate ~30 entries to change one.
 */
class ThemeRepository(private val sources: List<ThemeSource>) {
    constructor(vararg sources: ThemeSource) : this(sources.toList())

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

private fun RainbowTheme.overriddenBy(override: RainbowTheme) =
    RainbowTheme(
        prefix = prefix + override.prefix,
        base = base + override.base,
        arbitrary = override.arbitrary ?: arbitrary,
        important = override.important ?: important,
    )
