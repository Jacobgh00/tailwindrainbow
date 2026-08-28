package dev.tailwindrainbow.intellij.adapter.intellij.theme

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.extensions.ExtensionPointName
import dev.tailwindrainbow.intellij.adapter.theme.BuiltInThemes
import dev.tailwindrainbow.intellij.adapter.theme.ThemeContributor
import dev.tailwindrainbow.intellij.application.port.ThemeSource
import dev.tailwindrainbow.intellij.application.theme.SpecThemeSource
import dev.tailwindrainbow.intellij.application.theme.ThemeSpec
import dev.tailwindrainbow.intellij.domain.theme.RainbowTheme

object ContributedThemes : ThemeSource {
    private val EXTENSION_POINT = ExtensionPointName<ThemeContributor>("dev.tailwindrainbow.themeContributor")

    override fun themes(): Map<String, RainbowTheme> = SpecThemeSource(contributedSpecs(), BuiltInThemes).themes()

    private fun contributedSpecs(): List<ThemeSpec> =
        EXTENSION_POINT.extensionList.flatMap { contributor ->
            runCatching { contributor.themes() }
                .onFailure { thisLogger().warn("Theme contributor ${contributor.javaClass.name} failed", it) }
                .getOrDefault(emptyList())
        }
}
